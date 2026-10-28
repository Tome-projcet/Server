package com.Tome.tome.domain.auth.handler;

import com.Tome.tome.domain.auth.entity.RefreshToken;
import com.Tome.tome.domain.auth.repository.RefreshTokenRepository;
import com.Tome.tome.domain.auth.service.TokenService;
import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.global.config.jwt.TokenProvider;
import com.Tome.tome.global.util.CoolieUtil;
import com.Tome.tome.global.util.JsonResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

// 로그인 성공 시 리다이렉트 대신 JSON(액세스 토큰 + 첫 로그인 여부)으로 응답
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);

    @Value("${cookie.refresh-token.secure}")
    private boolean cookieSecure;

    @Value("${cookie.refresh-token.same-site}")
    private String cookieSameSite;

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginSuccessHandler(TokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();

        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION, TokenProvider.TYPE_REFRESH);
        saveRefreshToken(user.getId(), refreshToken);
        CoolieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken,
                (int) REFRESH_TOKEN_DURATION.toSeconds(), cookieSecure, cookieSameSite);

        String accessToken = tokenProvider.generateToken(user, TokenService.ACCESS_TOKEN_DURATION, TokenProvider.TYPE_ACCESS);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accessToken", accessToken);
        body.put("firstLogin", user.getfirst());
        JsonResponseUtil.write(response, HttpServletResponse.SC_OK, body);
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(saved -> saved.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));
        refreshTokenRepository.save(refreshToken);
    }
}
