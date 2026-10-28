package com.Tome.tome.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth", description = "로그인 / 토큰 API")
@RestController
public class CsrfController {

    @Operation(summary = "CSRF 토큰 발급",
            description = "Authorization 헤더 없이 보내는 POST(로그인, 회원가입, 쿠키 기반 로그아웃) 전에 호출하고, 받은 token을 X-XSRF-TOKEN 헤더에 넣습니다. 로그인하면 토큰이 바뀌므로 다시 받아야 합니다.")
    @SecurityRequirements
    @GetMapping("/api/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(Map.of("token", csrfToken.getToken()));
    }
}
