package com.ssafy.algogo.auth.service.jwt;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final UserRepository userRepository;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refresh-expiration}")
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
            throw new CustomException("Token 제작 과정에서 발생한 오류 (추후 수정)", ErrorCode.ACCESS_DENIED);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal(); // 이미 저장된 CustomUserContext에서 꺼내오기,
        Long userId = customUserDetails.getUserId();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));


        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("userId", userId)
                .claim("ip", ip)
                .claim("role", authorities)
                .claim("tokenType", tokenType)
                .setExpiration(new Date(System.currentTimeMillis() + validTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractTokenType(String token) {
        return getClaims(token).get("tokenType", String.class);
    }

    public void isValidateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
        } catch (MalformedJwtException e) {
            log.warn("INVALID JWT TOKEN : {}", e.getMessage());
            throw new CustomException("잘못된 JWT 서명입니다.", ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.warn("EXPIRED JWT TOKEN : {}", e.getMessage());
            throw new CustomException("토큰이 만료되었습니다.", ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("UNSUPPORTED JWT TOKEN : {}", e.getMessage());
            throw new CustomException("지원하지 않는 JWT 토큰입니다.", ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT CLAIM IS EMPTY : {}", e.getMessage());
            throw new CustomException("JWT 토큰이 비어있습니다.", ErrorCode.INVALID_TOKEN);
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Map<String, String> extractToken(String token, String... includedClaims) throws ExpiredJwtException {
        HashMap<String, String> claims = new HashMap<>();
        Claims body = getClaims(token);
        claims.put("subject", body.getSubject());

        for (String arg : includedClaims) {
            claims.put(arg, body.get(arg, String.class));
        }

        return claims;
    }

    public Long getUserIdFromAuthentication() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userId == null) {
            throw new RuntimeException(); // CustomException으로 수정
        }
        return userId;
    }

    public String getIpFromRequest(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public long getAccessTokenValidTime() {
        return this.accessTokenValidTime;
    }

    public long getRefreshTokenValidTime() {
        return this.refreshTokenValidTime;
    }

}
