package com.Tome.tome.controller;

import com.Tome.tome.catalog.BookApiException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = {BookCatalogController.class, RecommendationController.class, GenreController.class})
public class BookApiErrorHandler {
    @ExceptionHandler(BookApiException.class)
    public ResponseEntity<ProblemDetail> upstream(BookApiException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> invalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
}
