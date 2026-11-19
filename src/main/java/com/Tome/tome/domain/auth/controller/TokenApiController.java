package com.Tome.tome.domain.auth.controller;

import com.Tome.tome.domain.auth.dto.request.CreateAccessTokenRequest;
import com.Tome.tome.domain.auth.dto.response.CreateAccessTokenResponse;
import com.Tome.tome.domain.auth.port.in.TokenUseCase;
import com.Tome.tome.domain.auth.service.TokenService;
import com.Tome.tome.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth", description = "로그인 / 토큰 API")
@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final TokenUseCase tokenUseCase;
    @Value("${cookie.refresh-token.secure}")
    private boolean cookieSecure;

    @Value("${cookie.refresh-token.same-site}")
    private String cookieSameSite;

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
            @CookieValue(value = TokenService.REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieToken,
            @RequestBody(required = false) CreateAccessTokenRequest request){
        String refreshToken = cookieToken != null ? cookieToken : (request != null ? request.getRefreshToken() : null);
        String newAccessToken = tokenUseCase.createNewAccessToken(refreshToken);
        return ResponseEntity.status(201).body(new CreateAccessTokenResponse(newAccessToken));
    }

    @Operation(summary = "로그아웃",
            description = "DB의 리프레시 토큰과 refresh_token 쿠키를 삭제합니다. Bearer 토큰 없이 호출하면 X-XSRF-TOKEN 헤더가 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "403", description = "Bearer 없이 호출했는데 CSRF 토큰 누락")
    })
    @SecurityRequirements
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(value = TokenService.REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieToken,
            @AuthenticationPrincipal User user){
        tokenUseCase.logout(cookieToken, user != null ? user.getId() : null);

        ResponseCookie cookie = ResponseCookie.from(TokenService.REFRESH_TOKEN_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "로그아웃되었습니다"));
    }
}
