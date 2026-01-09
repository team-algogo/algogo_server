package com.ssafy.algogo.common.config;

import com.ssafy.algogo.auth.service.jwt.JwtAuthenticationTokenFilter;
import com.ssafy.algogo.auth.service.jwt.JwtTokenProvider;
import com.ssafy.algogo.auth.service.jwt.RedisJwtService;
import com.ssafy.algogo.auth.service.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisJwtService redisJwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(Customizer.withDefaults())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(new JwtAuthenticationTokenFilter(jwtTokenProvider, redisJwtService),
                UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth

                // 김성훈 프론트 체크 패스 API
                .requestMatchers(
                    "/api/v1/groups/**",
                    "/api/v1/auths/logout"
                ).permitAll()

                .requestMatchers(
                    "/api/v1/auths/login",
                    "/api/v1/groups/lists/**",
                    "/api/v1/users/signup",
                    "/api/v1/users/check/**",
                    "/api/v1/auths/forgot-password"
                ).permitAll()

                .requestMatchers(HttpMethod.GET,
                    "/api/v1/problem-sets/**",
                    "/api/v1/groups/lists",
                    "/api/v1/problems/**",
                    "/api/v1/submissions/**",
                    "/api/v1/submissions/trends").permitAll()
                // /api/v1/groups/lists/me -> ?? || /api/v1/problem-sets/me -> ??
                // /api/v1/submissions/me -> ??

                .requestMatchers("/test/auth/admin").hasRole("ADMIN") // 유저권한 테스트용
                .requestMatchers("/test/auth/user").hasRole("USER")
                .anyRequest().authenticated())
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true); // 쿠키를 포함한 요청 허용
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "https://localhost:3000",
            "http://localhost:5173",
            "https://localhost:5173",
            "http://43.201.209.14",
            "https://43.201.209.14",
            "https://www.algogo.kr"
        )); // 허용할 프론트엔드 도메인
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용할 메서드
        configuration.setAllowedHeaders(List.of("*")); //프론트엔드에서 요청을 보낼 때 포함할 수 있는 헤더
        configuration.setExposedHeaders(List.of("Authorization")); // 프론트에서 응답에서 조회할 수 있는 헤더

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정을 사용
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        //AuthenticationManager에 등록할 AuthenticationProvider를 새로 정의
        //DaoAuthenticationProvider는 DB 기반 인증을 수행하는 구현체
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        //등록할 AuthenticationProvider를 설정, 우리가 사용할 customUserDetailsService, 비밀번호 해시 객체를 세팅
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        //새로운 AuthenticationManager로서 ProviderManager구현체에 등록할 authenticationProvider를 넣고 설정
        return new ProviderManager(List.of(authenticationProvider));
    }

}
