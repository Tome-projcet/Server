package com.Tome.tome.domain.auth.controller;

import com.Tome.tome.domain.auth.dto.CreateAccessTokenRequest;
import com.Tome.tome.domain.auth.dto.CreateAccessTokenResponse;
import com.Tome.tome.domain.auth.port.in.TokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final TokenUseCase tokenUseCase;

    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest request){
        String newAcccessToken = tokenUseCase.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(201).body(new CreateAccessTokenResponse(newAcccessToken));
    }
}
