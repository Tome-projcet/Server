package com.Tome.tome.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "액세스 토큰 재발급 요청")
public class CreateAccessTokenRequest {
    @Schema(description = "로그인 시 발급된 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
