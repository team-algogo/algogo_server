package com.ssafy.algogo.user.service;


import com.ssafy.algogo.user.dto.request.CheckDuplicateEmailRequestDto;
import com.ssafy.algogo.user.dto.request.CheckDuplicateNicknameRequestDto;
import com.ssafy.algogo.user.dto.request.SignupRequestDto;
import com.ssafy.algogo.user.dto.response.CheckDuplicateEmailResponseDto;
import com.ssafy.algogo.user.dto.response.CheckDuplicateNicknameResponseDto;
import com.ssafy.algogo.user.dto.response.SignupResponseDto;
import com.ssafy.algogo.user.entity.User;

public interface UserService {

    SignupResponseDto signup(SignupRequestDto dto);

    CheckDuplicateEmailResponseDto isAvailableEmail(CheckDuplicateEmailRequestDto dto);

    CheckDuplicateNicknameResponseDto isAvailableNickname(CheckDuplicateNicknameRequestDto dto);

}
