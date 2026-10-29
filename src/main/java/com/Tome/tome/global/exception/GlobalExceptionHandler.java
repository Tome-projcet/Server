package com.Tome.tome.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return response(HttpStatus.BAD_REQUEST, e.getMessage(), "잘못된 요청입니다");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return response(HttpStatus.NOT_FOUND, e.getMessage(), "찾을 수 없습니다");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException e) {
        return response(HttpStatus.FORBIDDEN, e.getMessage(), "권한이 없습니다");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("요청 본문을 읽지 못했습니다: {}", e.getMessage());
        return response(HttpStatus.BAD_REQUEST, "요청 본문(JSON)이 없거나 형식이 올바르지 않습니다", null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException e) {
        return response(HttpStatus.BAD_REQUEST, "필수 파라미터가 없습니다: " + e.getParameterName(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return response(HttpStatus.BAD_REQUEST, "파라미터 형식이 올바르지 않습니다: " + e.getName(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다: " + e.getMethod(), null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMediaType(HttpMediaTypeNotSupportedException e) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResource(NoResourceFoundException e) {
        return response(HttpStatus.NOT_FOUND, "존재하지 않는 API입니다", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("데이터 무결성 오류", e);
        return response(HttpStatus.CONFLICT, "이미 존재하거나 저장할 수 없는 데이터입니다", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("처리되지 않은 오류", e);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다", null);
    }

    private ResponseEntity<Map<String, String>> response(HttpStatus status, String message, String defaultMessage) {
        String body = (message == null || message.isBlank()) ? defaultMessage : message;
        return ResponseEntity.status(status).body(Map.of("message", body));
    }
}
