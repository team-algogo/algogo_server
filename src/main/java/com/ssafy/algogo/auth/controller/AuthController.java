package com.ssafy.algogo.auth.controller;

import com.ssafy.algogo.auth.dto.request.LocalLoginRequestDto;
import com.ssafy.algogo.auth.dto.response.AuthResultDto;
import com.ssafy.algogo.auth.dto.response.LocalLoginResponseDto;
import com.ssafy.algogo.auth.service.AuthService;
import com.ssafy.algogo.auth.service.jwt.JwtTokenProvider;
import com.ssafy.algogo.auth.service.security.CookieUtils;
import com.ssafy.algogo.common.advice.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auths")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse login(@RequestBody LocalLoginRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        AuthResultDto authResultDto = authService.login(requestDto, request, response);

        CookieUtils.addTokenCookie(response, "accessToken", authResultDto.getTokenInfo().getAccessToken(), jwtTokenProvider.getAccessTokenValidTime());
        CookieUtils.addTokenCookie(response, "refreshToken", authResultDto.getTokenInfo().getRefreshToken(), jwtTokenProvider.getRefreshTokenValidTime());

        return SuccessResponse.success("로그인에 성공했습니다.", authResultDto.getLocalLoginResponseDto());
    }

}
