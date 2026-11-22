package com.Tome.tome.domain.auth.service;

import com.Tome.tome.domain.auth.entity.RefreshToken;
import com.Tome.tome.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken){
        return refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 리프레시 토큰입니다"));
    }

    @Transactional
    public void save(Long userId, String newRefreshToken){
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(saved -> saved.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void delete(String refreshToken, Long userId){
        if (refreshToken != null) {
            refreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(refreshTokenRepository::delete);
            return;
        }
        if (userId != null) {
            refreshTokenRepository.findByUserId(userId).ifPresent(refreshTokenRepository::delete);
        }
    }
}
