package com.Tome.tome.domain.book.service;

import com.Tome.tome.domain.book.infra.*;
import com.Tome.tome.global.config.BookApiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

/** 검색·상세 통합과 인기 도서 수집을 조정한다. 외부 HTTP 호출은 DB 저장 트랜잭션 밖에서 수행한다. */
@Service
@RequiredArgsConstructor
public class BookCatalogService {
    private final KakaoBookClient kakao;
    private final LibraryBookClient library;
    private final BookCatalogStore store;
    private final BookApiConfig config;
    private final com.Tome.tome.domain.book.repository.BookRepository books;
    /** 카카오가 실패하면 저장된 제목·저자로 대체한다. 대체 결과도 없을 때만 공급자 예외를 전달한다. */
    public List<ExternalBook> search(String query, int page, int size) {
        // 검색 결과마다 저장·추가 상세 요청을 하지 않아 지연과 API 사용량을 줄인다.
        try { return kakao.search(query, page, size); }
        catch (BookApiException e) {
            var cached = books.findByTitleContainingIgnoreCaseOrWriterContainingIgnoreCase(query, query,
                    org.springframework.data.domain.PageRequest.of(page - 1, size));
            if (cached.isEmpty()) throw e;
            return cached.stream().map(this::cached).toList();
        }
    }
    /** 정보나루 분류에 카카오 메타데이터를 합친다. 외부 결과가 없으면 기존 DB를 확인한다. */
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
    /** 정보나루 목록을 확보한 후 최대 20권만 카카오로 보완하고 일괄 저장한다. 보완 실패는 경고로 알린다. */
    public synchronized SyncResult syncPopular(LocalDate start, LocalDate end, int size) {
        List<ExternalBook> popular = library.popular(start, end, size);
        List<ExternalBook> enriched = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        // 선택적 카카오 보완은 최대 20회이며, 첫 장애·쿼터 오류 뒤에는 추가 요청을 멈춘다.
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
    /** 기존 DB 엔티티를 외부 공급자와 같은 응답 형식으로 변환한다. */
    private ExternalBook cached(com.Tome.tome.domain.book.entity.Book book) {
        return new ExternalBook(book.getIsbn13(), book.getTitle(), book.getWriter(), book.getBookurl(),
                book.getBookbuyurl(), book.getPublisher(), book.getDescription(),
                book.getGenre() == null ? "기타" : book.getGenre().getName(),
                book.getLoanCount() == null ? 0 : book.getLoanCount(), book.getSource());
    }
    @io.swagger.v3.oas.annotations.media.Schema(description = "인기 도서 수집 결과")
    public record SyncResult(@io.swagger.v3.oas.annotations.media.Schema(description = "신규 및 갱신을 포함한 저장 건수", example = "50") int saved,
                             @io.swagger.v3.oas.annotations.media.Schema(description = "대출 집계 시작일") LocalDate start,
                             @io.swagger.v3.oas.annotations.media.Schema(description = "대출 집계 종료일") LocalDate end,
                             @io.swagger.v3.oas.annotations.media.Schema(description = "카카오 보완 실패 등의 경고. 없으면 빈 배열") List<String> warnings) {}
}
