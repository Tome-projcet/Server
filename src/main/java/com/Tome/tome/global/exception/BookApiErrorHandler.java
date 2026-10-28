package com.Tome.tome.global.exception;

import com.Tome.tome.domain.book.infra.BookApiException;
import org.springframework.http.*;
import com.Tome.tome.domain.book.controller.BookCatalogController;
import com.Tome.tome.domain.book.controller.RecommendationController;
import com.Tome.tome.domain.genre.controller.GenreController;
import org.springframework.web.bind.annotation.*;

/** 담당 API에 한정해 입력 오류와 공급자 장애를 ProblemDetail 응답으로 변환한다. 다른 팀원의 예외 처리는 변경하지 않는다. */
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {BookCatalogController.class, RecommendationController.class, GenreController.class})
public class BookApiErrorHandler {
    /** 공급자 설정·연결·응답 오류는 503으로 알린다. */
    @ExceptionHandler(BookApiException.class)
    public ResponseEntity<ProblemDetail> upstream(BookApiException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage()));
    }
    /** 검색 조건·ISBN·관심 장르 등의 잘못된 입력은 400으로 알린다. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> invalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
}
