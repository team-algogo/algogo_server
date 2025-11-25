package com.ssafy.algogo.auth.controller;

import com.ssafy.algogo.auth.dto.request.LocalLoginRequestDto;
import com.ssafy.algogo.auth.dto.response.LocalLoginResponseDto;
import com.ssafy.algogo.auth.service.AuthService;
import com.ssafy.algogo.auth.service.jwt.JwtTokenProvider;
import com.ssafy.algogo.common.advice.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auths")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public SuccessResponse login(@RequestBody LocalLoginRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        LocalLoginResponseDto responseDto = authService.login(requestDto, request, response);
        return SuccessResponse.success("로그인에 성공했습니다.", responseDto);
    }

}
