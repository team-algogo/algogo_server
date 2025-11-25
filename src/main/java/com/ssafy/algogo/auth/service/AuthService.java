package com.ssafy.algogo.auth.service;

import com.ssafy.algogo.auth.dto.request.LocalLoginRequestDto;
import com.ssafy.algogo.auth.dto.response.LocalLoginResponseDto;
import com.ssafy.algogo.user.dto.response.SignupResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LocalLoginResponseDto login(LocalLoginRequestDto dto, HttpServletRequest request, HttpServletResponse response);

}
