package com.Tome.tome.domain.user.dto.response;

public record LoginResult(
        String accessToken,
        String refreshToken,
        Boolean firstLogin
) {
}
