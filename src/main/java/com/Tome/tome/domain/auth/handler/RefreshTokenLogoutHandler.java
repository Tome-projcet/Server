package com.Tome.tome.domain.auth.handler;

import com.Tome.tome.domain.auth.repository.RefreshTokenRepository;
import com.Tome.tome.global.config.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.Tome.tome.global.util.CoolieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

// 로그아웃 시 DB의 리프레시 토큰을 지워서, 로그아웃 후에는 재발급이 안 되게 함
@Component
@RequiredArgsConstructor
public class RefreshTokenLogoutHandler implements LogoutHandler {

    private static final String TOKEN_PREFIX = "Bearer ";

    @Value("${cookie.refresh-token.secure}")
    private boolean cookieSecure;

    @Value("${cookie.refresh-token.same-site}")
    private String cookieSameSite;

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 발급할 때와 같은 secure/sameSite 속성으로 지워야 다른 도메인의 프론트에서도 쿠키가 삭제된다
        CoolieUtil.addCookie(response, LoginSuccessHandler.REFRESH_TOKEN_COOKIE_NAME, "", 0, cookieSecure, cookieSameSite);

        String refreshToken = findRefreshTokenCookie(request);
        if (refreshToken != null) {
            refreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(refreshTokenRepository::delete);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            String accessToken = header.substring(TOKEN_PREFIX.length());
            if (tokenProvider.validToken(accessToken, TokenProvider.TYPE_ACCESS)) {
                refreshTokenRepository.findByUserId(tokenProvider.getUserId(accessToken))
                        .ifPresent(refreshTokenRepository::delete);
            }
        }
    }

    private String findRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (LoginSuccessHandler.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
