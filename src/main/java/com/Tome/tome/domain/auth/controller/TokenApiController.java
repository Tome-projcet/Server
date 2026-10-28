package com.Tome.tome.domain.auth.controller;

import com.Tome.tome.domain.auth.dto.request.CreateAccessTokenRequest;
import com.Tome.tome.domain.auth.dto.response.CreateAccessTokenResponse;
import com.Tome.tome.domain.auth.handler.LoginSuccessHandler;
import com.Tome.tome.domain.auth.port.in.TokenUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로그인 / 토큰 API")
@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final TokenUseCase tokenUseCase;

    @Operation(summary = "액세스 토큰 발급",
            description = "refresh_token 쿠키 또는 body의 refreshToken으로 2시간짜리 액세스 토큰을 발급합니다. 쿠키가 있으면 쿠키를 먼저 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "발급 성공"),
            @ApiResponse(responseCode = "400", description = "토큰이 없거나 만료됐거나 DB에 없는 경우",
                    content = @Content(schema = @Schema(example = "{\"message\": \"Unexpected token\"}")))
    })
    @SecurityRequirements
    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
            @Parameter(description = "로그인 시 발급된 리프레시 토큰 쿠키")
            @CookieValue(value = LoginSuccessHandler.REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieToken,
            @RequestBody(required = false) CreateAccessTokenRequest request){
        String refreshToken = cookieToken != null ? cookieToken : (request != null ? request.getRefreshToken() : null);
        String newAccessToken = tokenUseCase.createNewAccessToken(refreshToken);
        return ResponseEntity.status(201).body(new CreateAccessTokenResponse(newAccessToken));
    }
}
