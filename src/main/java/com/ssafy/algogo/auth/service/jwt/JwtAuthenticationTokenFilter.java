package com.ssafy.algogo.auth.service.jwt;

import com.ssafy.algogo.auth.service.security.CookieUtils;
import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final int BEARER_PREFIX_COUNT = 7;

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisJwtService redisJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String accessToken =
            (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(
                BEARER_PREFIX_COUNT) : null;
        String refreshToken = CookieUtils.getTokenFromCookies("refreshToken", request);

        try {
            if (accessToken != null) {
                // 1. AT 검증 시도
                try {
                    jwtTokenProvider.isValidateToken(accessToken);
                    authenticateWithAccessToken(accessToken, request);
                } catch (Exception e) {
                    // 2. AT가 만료되었거나 문제가 있다면 RT 확인
                    log.debug("Access Token invalid, checking Refresh Token...");
                    if (refreshToken != null) {
                        jwtTokenProvider.isValidateToken(refreshToken);
                        reissueTokens(refreshToken, request, response);
                    } else {
                        throw e; // RT도 없으면 에러 던짐
                    }
                }
            } else if (refreshToken != null) {
                // AT가 아예 없는 경우 RT로 인증 시도
                jwtTokenProvider.isValidateToken(refreshToken);
                reissueTokens(refreshToken, request, response);
            }
        } catch (Exception e) {
            log.debug("JWT Authentication failed: {}", e.getMessage());
            // 필요 시 여기서 401 에러를 직접 응답하거나 context를 비웁니다.
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateWithAccessToken(String accessToken, HttpServletRequest request) {
        Map<String, String> token = jwtTokenProvider.extractToken(accessToken, "ip", "role");

        String ip = token.get("ip");
        String currentIp = jwtTokenProvider.getIpFromRequest(request);

        if (ip == null || !ip.equals(currentIp)) {
            log.debug("IP mismatch - Token IP: {}, Request IP: {}", ip, currentIp);
            throw new CustomException("IP 불일치", ErrorCode.ACCESS_DENIED);
        }

        Long userId = Long.valueOf(token.get("subject"));
        String role = token.get("role");
        Collection<? extends GrantedAuthority> authorities = (role != null && !role.isEmpty())
            ? Collections.singletonList(new SimpleGrantedAuthority(role))
            : Collections.emptyList();

        // 로그아웃 검증
        if (!redisJwtService.exists(userId)) {
            log.error("이미 로그아웃 한 사용자입니다. userId: {}", userId);
            throw new CustomException("이미 로그아웃한 사용자입니다.", ErrorCode.ACCESS_DENIED);
        }

        CustomUserDetails principal = new CustomUserDetails(userId, userId.toString(), "",
            authorities);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, authorities)
        );

        log.debug("Authenticated with Access Token - User: {}, IP: {}", userId, ip);
    }

    private void reissueTokens(String refreshToken, HttpServletRequest request,
        HttpServletResponse response) {
        Map<String, String> token = jwtTokenProvider.extractToken(refreshToken, "ip", "role");

        Long userId = Long.valueOf(token.get("subject"));
        String tokenIp = token.get("ip");
        String currentIp = jwtTokenProvider.getIpFromRequest(request);
        String role = token.get("role");

        // IP 검증
        if (tokenIp == null || !tokenIp.equals(currentIp)) {
            log.debug("Refresh Token IP mismatch - User: {}", userId);
            throw new CustomException("IP 불일치", ErrorCode.ACCESS_DENIED);
        }

        // Redis에서 저장된 RT와 비교 검증
        if (!redisJwtService.validate(userId, refreshToken, currentIp)) {
            log.debug("Invalid Refresh Token in Redis - User: {}", userId);
            throw new CustomException("유효하지 않은 Refresh Token", ErrorCode.EXPIRED_TOKEN);
        }

        // 로그아웃 검증
        if (!redisJwtService.exists(userId)) {
            log.error("이미 로그아웃 한 사용자입니다. userId: {}", userId);
            throw new CustomException("이미 로그아웃한 사용자입니다.", ErrorCode.ACCESS_DENIED);
        }

        // 새로운 인증 객체 생성
        Collection<? extends GrantedAuthority> authorities = (role != null && !role.isEmpty())
            ? Collections.singletonList(new SimpleGrantedAuthority(role))
            : Collections.emptyList();

        CustomUserDetails principal = new CustomUserDetails(userId, userId.toString(), "",
            authorities);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication, currentIp);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication, currentIp);

        // Redis 업데이트
        redisJwtService.save(userId, newRefreshToken, currentIp);

        // 쿠키/헤더에 새 토큰 설정
        response.setHeader("Authorization", newAccessToken);
        CookieUtils.addTokenCookie(response, "refreshToken", newRefreshToken,
            jwtTokenProvider.getRefreshTokenValidTime());

        // SecurityContext에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Tokens reissued - User: {}, IP: {}", userId, currentIp);
    }


}
