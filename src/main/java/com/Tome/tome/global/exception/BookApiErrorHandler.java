package com.Tome.tome.global.exception;

import com.Tome.tome.domain.book.infra.BookApiException;
import org.springframework.http.*;
import com.Tome.tome.domain.book.controller.BookCatalogController;
import com.Tome.tome.domain.book.controller.RecommendationController;
import com.Tome.tome.domain.genre.controller.GenreController;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 담당 API에 한정해 입력 오류와 공급자 장애를 다른 API와 같은 {"message": ...} 형식으로 변환한다. */
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {BookCatalogController.class, RecommendationController.class, GenreController.class})
public class BookApiErrorHandler {
    /** 공급자 설정·연결·응답 오류는 503으로 알린다. */
    @ExceptionHandler(BookApiException.class)
    public ResponseEntity<Map<String, String>> upstream(BookApiException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", message(e, "도서 API를 사용할 수 없습니다")));
    }
    /** 검색 조건·ISBN·관심 장르 등의 잘못된 입력은 400으로 알린다. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", message(e, "잘못된 요청입니다")));
    }
    /** 예외 메시지가 비어 있으면 기본 메시지를 쓴다. Map.of는 null 값을 받지 않는다. */
    private String message(Exception e, String defaultMessage) {
        return e.getMessage() == null || e.getMessage().isBlank() ? defaultMessage : e.getMessage();
    }
}
