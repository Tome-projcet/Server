package com.Tome.tome.domain.auth.controller;

import com.Tome.tome.domain.auth.dto.request.CreateAccessTokenRequest;
import com.Tome.tome.domain.auth.dto.response.CreateAccessTokenResponse;
import com.Tome.tome.domain.auth.handler.LoginSuccessHandler;
import com.Tome.tome.domain.auth.port.in.TokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final TokenUseCase tokenUseCase;

    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
            @CookieValue(value = LoginSuccessHandler.REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieToken,
            @RequestBody(required = false) CreateAccessTokenRequest request){
        String refreshToken = cookieToken != null ? cookieToken : (request != null ? request.getRefreshToken() : null);
        String newAccessToken = tokenUseCase.createNewAccessToken(refreshToken);
        return ResponseEntity.status(201).body(new CreateAccessTokenResponse(newAccessToken));
    }
}
