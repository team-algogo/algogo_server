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

        /**
         *  Security Config 부분에서 만약 Token이 필요한 로직이라면 해당 Filter를 탄다.
         *  지금 현재는 개발단계이기때문에 모든 URI를 permitAll해둔 상태, 그렇기때문에 여기서 at, rt null체크를 해버리면 모두 걸리게된다,
         *  여기서 지금 null체크를 해버리게되면 토큰이 필요없는 로직에서도 null이여서 오류가 터질 것, 그렇기때문에 나중에 꼭 토큰유무에따른 uri를 나누고
         *  filter를 걸치는 로직에 대해서는 null체크를 하는 부분을 체크해서 에러처리를 해줘야한다,
         */

        try {
//            if (accessToken == null && refreshToken == null) {
//                throw new CustomException("JWT 토큰이 비어있습니다.", ErrorCode.EMPTY_TOKEN);
//            }
            if (accessToken != null) {
                jwtTokenProvider.isValidateToken(accessToken);
                authenticateWithAccessToken(accessToken, request);
                filterChain.doFilter(request, response);
                return;
            }

            if (refreshToken != null) {
                jwtTokenProvider.isValidateToken(refreshToken);
                reissueTokens(refreshToken, request, response);
                filterChain.doFilter(request, response);
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT Filter Error : {}", e.getMessage());
            throw new CustomException("예상치 못한 서버 에러입니다. - 관리자에게 따지셔야합니다. Mady By 김성훈", ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void authenticateWithAccessToken(String accessToken, HttpServletRequest request) {
        Map<String, String> token = jwtTokenProvider.extractToken(accessToken, "ip", "role");

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

        // 로그아웃 검증
        if (!redisJwtService.exists(userId)) {
            log.error("이미 로그아웃 한 사용자입니다. userId: {}", userId);
            throw new CustomException("이미 로그아웃한 사용자입니다.", ErrorCode.ACCESS_DENIED);
        }

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
