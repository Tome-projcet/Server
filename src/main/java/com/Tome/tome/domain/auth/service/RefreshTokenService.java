package com.Tome.tome.domain.auth.service;

import com.Tome.tome.domain.auth.entity.RefreshToken;
import com.Tome.tome.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken){
        return refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new IllegalArgumentException("Unexpected token"));
    }
}
