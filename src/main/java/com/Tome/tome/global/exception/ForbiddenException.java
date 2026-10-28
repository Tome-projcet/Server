package com.Tome.tome.global.exception;

// 본인 글/댓글이 아닌 것을 수정·삭제하려 할 때 던지는 예외 (403)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
