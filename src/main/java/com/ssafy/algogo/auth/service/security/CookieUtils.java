package com.ssafy.algogo.auth.service.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtils {

    public static void addTokenCookie(HttpServletResponse response, String name, String value, long maxAgeMs) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true) // js접근 차단 (XSS 방어)
                .secure(true) // HTTPS만 가능, 일단 쿠키에 넣으면 안되려나,
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeMs / 1000)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static String getTokenFromCookies(String tokenType, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> tokenCookie = Arrays.stream(cookies)
                    .filter(cookie -> tokenType.equals(cookie.getName()))
                    .findFirst();
            if (tokenCookie.isPresent())
                return tokenCookie.get().getValue();
        }
        return null;
    }

    public static void deleteTokenCookie(HttpServletResponse response) {
        addTokenCookie(response, "refreshToken", "", 0L);
    }
}
