package com.Tome.tome.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "액세스 토큰 발급 응답")
public class CreateAccessTokenResponse {
    @Schema(description = "API 호출에 쓰는 액세스 토큰 (2시간 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
}
