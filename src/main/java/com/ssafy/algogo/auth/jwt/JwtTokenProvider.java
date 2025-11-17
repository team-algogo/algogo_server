package com.ssafy.algogo.auth.jwt;

import com.ssafy.algogo.auth.service.CustomUserDetailsService;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.secretKey")
    private String secretKey;

    @Value("${jwt.accessExpiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenValidTime;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createAccessToken(User user, String ip) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("ip", ip)
                .claim("role", user.getUserRole())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(User user, String ip) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("ip", ip)
                .claim("role", user.getUserRole())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


}
