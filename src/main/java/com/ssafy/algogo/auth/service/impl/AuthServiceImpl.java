package com.ssafy.algogo.auth.service.impl;

import com.ssafy.algogo.auth.dto.request.LocalLoginRequestDto;
import com.ssafy.algogo.auth.dto.response.AuthResultDto;
import com.ssafy.algogo.auth.dto.response.LocalLoginResponseDto;
import com.ssafy.algogo.auth.dto.response.MeResponseDto;
import com.ssafy.algogo.auth.dto.response.TokenInfo;
import com.ssafy.algogo.auth.service.AuthService;
import com.ssafy.algogo.auth.service.jwt.JwtTokenProvider;
import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.auth.service.jwt.RedisJwtService;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisJwtService redisJwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResultDto login(LocalLoginRequestDto dto, HttpServletRequest request, HttpServletResponse response) {

        User user = null;

        user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException("존재하는 이메일이 없습니다.", ErrorCode.INVALID_LOGIN_CREDENTIALS));

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException("비밀번호가 일치하지 않습니다.", ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        // 이 부분이, email, pwd로 토큰을 만들고
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());

        // 토큰을 이용해서 securityFilterChain에서 검사를해서 만들고
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        String ip = jwtTokenProvider.getIpFromRequest(request);
        // 아래에서 토큰을 authentication 기반으로 만든다,
        String accessToken = jwtTokenProvider.createAccessToken(authentication, ip);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, ip);

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        redisJwtService.save(customUserDetails.getUserId(), refreshToken, ip); // redist에 rt저장

        user = userRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getUserId())
                .orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다", ErrorCode.ACCESS_DENIED));

        return AuthResultDto.builder()
                .localLoginResponseDto(LocalLoginResponseDto.from(user))
                .tokenInfo(new TokenInfo(accessToken, refreshToken))
                .build();
    }

    @Override
    public void logout(Long userId) {
        redisJwtService.delete(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public MeResponseDto me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다", ErrorCode.ACCESS_DENIED));

        return MeResponseDto.from(user);
    }

}
