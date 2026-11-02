package com.Tome.tome.domain.user.service;

import com.Tome.tome.domain.book.dto.BookResponseDto;
import com.Tome.tome.domain.book.repository.BookRepository;
import com.Tome.tome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;

/** 외부 HTTP 요청과 분리한 짧은 트랜잭션에서 인생 책 연결을 읽고 변경한다. */
@Service
@RequiredArgsConstructor
public class LifeBookStore {
    private final UserRepository users;
    private final BookRepository books;

    /** ID로 사용자를 다시 조회해 JWT에 실린 과거 사용자 상태에 의존하지 않는다. */
    @Transactional(readOnly = true)
    public Optional<BookResponseDto> get(Long userId) {
        var user = users.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다"));
        return Optional.ofNullable(user.getLifeBook()).map(BookResponseDto::new);
    }

    /** 다중 장르를 포함한 DTO를 트랜잭션 안에서 만들어 지연 로딩과 순환 직렬화를 방지한다. */
    @Transactional
    public BookResponseDto set(Long userId, Long bookId) {
        var user = users.findByIdForLifeBookUpdate(userId).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다"));
        var book = books.findById(bookId).orElseThrow(() -> new NoSuchElementException("책을 찾을 수 없습니다"));
        user.updateLifeBook(book);
        return new BookResponseDto(book);
    }

    /** 공유 도서를 삭제하지 않고 현재 사용자의 선택만 해제한다. 반복 호출해도 성공한다. */
    @Transactional
    public void clear(Long userId) {
        var user = users.findByIdForLifeBookUpdate(userId).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다"));
        user.updateLifeBook(null);
    }
}
