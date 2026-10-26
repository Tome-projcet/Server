package com.Tome.tome.domain.auth.port.in;

public interface TokenUseCase {
    String createNewAccessToken(String refreshToken);
}
