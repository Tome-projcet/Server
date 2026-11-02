package com.Tome.tome.domain.book.service;

import com.Tome.tome.domain.book.infra.Isbn;
import com.Tome.tome.domain.book.infra.BookApiException;
import com.Tome.tome.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

/** 독후감과 인생 책이 동일한 ISBN 도서를 공유하도록 조회·API 수집 경로를 통합한다. */
@Service
@RequiredArgsConstructor
public class BookSelectionService {
    private final BookRepository books;
    private final BookCatalogService catalog;
    private final BookCatalogStore store;

    /** 검색 응답의 ISBN만 받는다. 제목·표지 등은 서버가 조회하며 외부 호출 중 사용자 행 잠금을 잡지 않는다. */
    public Long select(String isbn) {
        String normalized = Isbn.normalize(isbn);
        if (normalized.isBlank()) throw new IllegalArgumentException("유효한 ISBN을 입력하세요.");
        var existing = books.findByIsbn13(normalized);
        if (existing.isPresent()) return existing.get().getId();
        var found = catalog.details(normalized);
        if (!normalized.equals(found.isbn13())) throw new BookApiException("조회한 책의 ISBN이 선택한 ISBN과 다릅니다.");
        try {
            store.upsert(List.of(found));
        } catch (DataIntegrityViolationException e) {
            // 다른 요청이 같은 책을 먼저 저장했다. 아래에서 그 책을 다시 조회해 사용한다.
        }
        return books.findByIsbn13(normalized).orElseThrow(() -> new NoSuchElementException("책 정보를 저장하지 못했습니다")).getId();
    }
}
