package com.Tome.tome.domain.user.service;

import com.Tome.tome.domain.book.dto.BookResponseDto;
import com.Tome.tome.domain.book.service.BookSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** API 도서 선택 후 인생 책 연결을 갱신한다. 외부 조회 실패 시 기존 인생 책은 유지된다. */
@Service
@RequiredArgsConstructor
public class LifeBookService {
    private final BookSelectionService selection;
    private final LifeBookStore store;

    /** 미저장 도서는 API에서 수집하고, 이미 저장된 도서는 키·외부 연결 없이 재사용한다. */
    public BookResponseDto set(Long userId, String isbn) {
        Long bookId = selection.select(isbn);
        return store.set(userId, bookId);
    }
}
