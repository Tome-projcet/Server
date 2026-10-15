package com.Tome.tome.service;

import com.Tome.tome.catalog.*;
import com.Tome.tome.config.BookApiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookCatalogService {
    private final KakaoBookClient kakao;
    private final LibraryBookClient library;
    private final BookCatalogStore store;
    private final BookApiConfig config;
    private final com.Tome.tome.repository.BookRepository books;
    public List<ExternalBook> search(String query, int page, int size) {
        // Search does not import every result or make one extra API request per result.
        try { return kakao.search(query, page, size); }
        catch (BookApiException e) {
            var cached = books.findByTitleContainingIgnoreCaseOrWriterContainingIgnoreCase(query, query,
                    org.springframework.data.domain.PageRequest.of(page - 1, size));
            if (cached.isEmpty()) throw e;
            return cached.stream().map(this::cached).toList();
        }
    }
    public ExternalBook details(String isbn) {
        String normalized = Isbn.normalize(isbn);
        if (normalized.isBlank()) throw new IllegalArgumentException("유효한 ISBN을 입력하세요.");
        ExternalBook kakaoBook = null, libraryBook = null;
        boolean failed = false;
        if (!config.getKakaoKey().isBlank()) {
            try { kakaoBook = kakao.findByIsbn(normalized).orElse(null); }
            catch (BookApiException e) { failed = true; }
        }
        if (!config.getLibraryKey().isBlank()) {
            try { libraryBook = library.details(normalized).orElse(null); }
            catch (BookApiException e) { failed = true; }
        }
        if (libraryBook != null) return libraryBook.enrichedWith(kakaoBook);
        if (kakaoBook != null) return kakaoBook;
        var stored = books.findByIsbn13(normalized);
        if (stored.isPresent()) return cached(stored.get());
        if (failed || config.getKakaoKey().isBlank() && config.getLibraryKey().isBlank())
            throw new BookApiException("도서 API를 사용할 수 없습니다. API 키와 연결 상태를 확인하세요.");
        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "도서를 찾을 수 없습니다.");
    }
    public synchronized SyncResult syncPopular(LocalDate start, LocalDate end, int size) {
        List<ExternalBook> popular = library.popular(start, end, size);
        List<ExternalBook> enriched = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        // Optional enrichment is bounded and stops on the first upstream failure (including quota exhaustion).
        boolean enrich = !config.getKakaoKey().isBlank();
        int enrichmentRequests = 0;
        for (ExternalBook book : popular) {
            ExternalBook details = null;
            if (enrich && enrichmentRequests < 20) {
                enrichmentRequests++;
                try { details = kakao.findByIsbn(book.isbn13()).orElse(null); }
                catch (BookApiException e) {
                    warnings.add("카카오 정보 보완을 중단하고 정보나루 데이터로 저장했습니다.");
                    enrich = false;
                }
            }
            enriched.add(book.enrichedWith(details));
        }
        return new SyncResult(store.upsert(enriched), start, end, List.copyOf(warnings));
    }
    private ExternalBook cached(com.Tome.tome.domain.Book book) {
        return new ExternalBook(book.getIsbn13(), book.getTitle(), book.getWriter(), book.getBookurl(),
                book.getBookbuyurl(), book.getPublisher(), book.getDescription(),
                book.getGenre() == null ? "기타" : book.getGenre().getName(),
                book.getLoanCount() == null ? 0 : book.getLoanCount(), book.getSource());
    }
    public record SyncResult(int saved, LocalDate start, LocalDate end, List<String> warnings) {}
}
