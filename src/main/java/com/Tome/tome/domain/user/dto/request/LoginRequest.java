package com.Tome.tome.domain.user.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
