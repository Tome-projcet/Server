package com.Tome.tome.domain.auth.handler;

import com.Tome.tome.global.util.JsonResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        JsonResponseUtil.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                Map.of("message", "이메일 또는 비밀번호가 올바르지 않습니다"));
    }
}
