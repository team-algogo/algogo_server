package com.ssafy.algogo.auth.service.jwt;

import com.ssafy.algogo.auth.service.AuthService;
import com.ssafy.algogo.auth.service.security.CookieUtils;
import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.user.entity.UserRole;
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
public class JwtAccessTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisJwtService redisJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = CookieUtils.getTokenFromCookies("accessToken", request);

        if (accessToken == null || accessToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, String> token = jwtTokenProvider.extractToken(accessToken, "ip", "role");

            String userIdStr = token.get("subject");
            Long userId = Long.valueOf(userIdStr);
            String role = token.get("role");
            Collection<? extends GrantedAuthority> authorities = (role != null)
                    ? Collections.singletonList(new SimpleGrantedAuthority(role))
                    : Collections.emptyList();

            String ip = token.get("ip");

            if (ip.isEmpty() || !ip.equals(jwtTokenProvider.getIpFromRequest(request))) {
                throw new RuntimeException("Strange Access Token");
            }

            CustomUserDetails principal = new CustomUserDetails(userId, userId.toString(), "", authorities);

            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));

            log.info("ACCESS USER INFO | User : {}, Role : {}", userId, authorities);

        } catch (ExpiredJwtException e) {
            log.error(e.getLocalizedMessage().split("\\.")[0]);
        } catch (Exception e) {
            log.error("Error : , {}", e.getLocalizedMessage());
        }
        filterChain.doFilter(request, response);
    }
}
