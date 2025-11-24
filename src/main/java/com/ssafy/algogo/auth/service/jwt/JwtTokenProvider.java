package com.ssafy.algogo.auth.service.jwt;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.auth.service.security.CustomUserDetailsService;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.net.UnknownServiceException;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final UserRepository userRepository;

    @Value("${jwt.secretKey")
    private String secretKey;

    @Value("${jwt.accessExpiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenValidTime;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createAccessToken(Authentication authentication, String ip) {
        try {
            return createToken(authentication, ip, "access", accessTokenValidTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String createRefreshToken(Authentication authentication, String ip) {
        try {
            return createToken(authentication, ip, "refresh", refreshTokenValidTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createToken(Authentication authentication, String ip, String tokenType, long validTime) {

        if (!(authentication.getPrincipal().getClass() == CustomUserDetails.class)) {
            throw new CustomException("Token 제작 과정에서 발생한 오류 (추후 수정)", ErrorCode.BAD_REQUEST_ERROR);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal(); // 이미 저장된 CustomUserContext에서 꺼내오기,
        Long userId = customUserDetails.getUserId();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));


        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("ip", ip)
                .claim("role", authorities)
                .claim("tokenType", tokenType)
                .setExpiration(new Date(System.currentTimeMillis() + validTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.warn("INVALID JWT TOKEN : {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("EXPIRED JWT TOKEN : {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("UNSUPPORTED JWT TOKEN : {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT CLAIM IS EMPTY : {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("INVALID JWT SIGNATURE : {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}
