package com.ssafy.algogo.auth.service.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public void addTokenCookie(HttpServletResponse response, String name, String value, long maxAgeMs) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true) // js접근 차단 (XSS 방어)
                .secure(true) // HTTPS만 가능, 일단 쿠키에 넣으면 안되려나,
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeMs / 1000)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteTokenCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
