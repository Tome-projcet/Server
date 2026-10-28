package com.Tome.tome.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class CoolieUtil {
    // 배포 환경(프론트와 서버 도메인이 다름)에서는 secure=true, sameSite=None 이어야 브라우저가 쿠키를 보낸다
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge,
                                 boolean secure, String sameSite){
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge){
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request,HttpServletResponse response, String name){
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return;
        }

        for(Cookie cookie : cookies){
            if(name.equals(cookie.getName())){
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }
}
