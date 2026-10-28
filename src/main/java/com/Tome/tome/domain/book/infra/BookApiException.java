package com.Tome.tome.domain.book.infra;

/** 외부 도서 공급자의 설정·연결·응답 문제를 표현한다. 컨트롤러에서 503으로 변환한다. */
public class BookApiException extends RuntimeException {
    /** 키나 원문 응답을 포함하지 않은 안전한 설명만 보관한다. */
    public BookApiException(String message) { super(message); }
}
