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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisJwtService redisJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = CookieUtils.getTokenFromCookies("accessToken", request);
        String refreshToken = CookieUtils.getTokenFromCookies("refreshToken", request);

        try {
            if (accessToken != null && jwtTokenProvider.isValidateToken(accessToken)) {
                authenticateWithAccessToken(accessToken, request);
                filterChain.doFilter(request, response);
                return;
            }

            if (refreshToken != null && jwtTokenProvider.isValidateToken(refreshToken)) {
                reissueTokens(refreshToken, request, response);
                filterChain.doFilter(request, response);
                return;
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("All Token Expired : {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT Filter Error : {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateWithAccessToken(String accessToken, HttpServletRequest request) {
        Map<String, String> token = jwtTokenProvider.extractToken(accessToken);

        String ip = token.get("ip");
        String currentIp = jwtTokenProvider.getIpFromRequest(request);

        if (ip == null || !ip.equals(currentIp)) {
            log.warn("IP mismatch - Token IP: {}, Request IP: {}", ip, currentIp);
            throw new CustomException("IP 불일치", ErrorCode.ACCESS_DENIED);
        }

        Long userId = Long.valueOf(token.get("subject"));
        String role = token.get("role");
        Collection<? extends GrantedAuthority> authorities = (role != null && !role.isEmpty())
                ? Collections.singletonList(new SimpleGrantedAuthority(role))
                : Collections.emptyList();

        CustomUserDetails principal = new CustomUserDetails(userId, userId.toString(), "", authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities)
        );

        log.info("Authenticated with Access Token - User: {}, IP: {}", userId, ip);
    }

    private void reissueTokens(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> token = jwtTokenProvider.extractToken(refreshToken, "ip", "role");

        Long userId = Long.valueOf(token.get("subject"));
        String tokenIp = token.get("ip");
        String currentIp = jwtTokenProvider.getIpFromRequest(request);
        String role = token.get("role");

        // IP 검증
        if (tokenIp == null || !tokenIp.equals(currentIp)) {
            log.warn("Refresh Token IP mismatch - User: {}", userId);
            throw new CustomException("IP 불일치", ErrorCode.ACCESS_DENIED);
        }

        // Redis에서 저장된 RT와 비교 검증
        if (!redisJwtService.validate(userId, refreshToken, currentIp)) {
            log.warn("Invalid Refresh Token in Redis - User: {}", userId);
            throw new CustomException("유효하지 않은 Refresh Token", ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // 새로운 인증 객체 생성
        Collection<? extends GrantedAuthority> authorities = (role != null && !role.isEmpty())
                ? Collections.singletonList(new SimpleGrantedAuthority(role))
                : Collections.emptyList();

        CustomUserDetails principal = new CustomUserDetails(userId, userId.toString(), "", authorities);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication, currentIp);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication, currentIp);

        // Redis 업데이트
        redisJwtService.save(userId, newRefreshToken, currentIp);

        // 쿠키에 새 토큰 설정
        CookieUtils.addTokenCookie(response, "accessToken", newAccessToken,
                jwtTokenProvider.getAccessTokenValidTime());
        CookieUtils.addTokenCookie(response, "refreshToken", newRefreshToken,
                jwtTokenProvider.getRefreshTokenValidTime());

        // SecurityContext에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("Tokens reissued - User: {}, IP: {}", userId, currentIp);
    }
}
