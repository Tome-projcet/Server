package com.Tome.tome.domain.auth.service;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.auth.port.in.TokenUseCase;
import com.Tome.tome.domain.user.port.in.UserUseCase;
import com.Tome.tome.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenService implements TokenUseCase {
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(2);

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserUseCase userUseCase;

    @Override
    public String createNewAccessToken(String refreshToken){
        if(!tokenProvider.validToken(refreshToken, TokenProvider.TYPE_REFRESH)){
            throw new IllegalArgumentException("유효하지 않거나 만료된 리프레시 토큰입니다");
        }

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        User user = userUseCase.findById(userId);

        return tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION, TokenProvider.TYPE_ACCESS);
    }
}
