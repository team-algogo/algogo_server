package com.ssafy.algogo.auth.service;

import com.ssafy.algogo.auth.dto.request.LocalLoginRequestDto;
import com.ssafy.algogo.auth.dto.response.LocalLoginResponseDto;
import com.ssafy.algogo.user.entity.User;

public interface AuthService {

    LocalLoginResponseDto login(LocalLoginRequestDto dto, String ip);

}
