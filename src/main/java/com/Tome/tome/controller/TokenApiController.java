package com.Tome.tome.controller;


import com.Tome.tome.dto.CreateAccessTokenRequest;
import com.Tome.tome.dto.CreateAccessTokenResponse;
import com.Tome.tome.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final TokenService tokenService;

    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest request){
        String newAcccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(201).body(new CreateAccessTokenResponse(newAcccessToken));
    }
}
