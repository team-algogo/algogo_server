package com.ssafy.algogo.user.service;


import com.ssafy.algogo.user.dto.request.CheckDuplicateEmailRequestDto;
import com.ssafy.algogo.user.dto.request.CheckDuplicateNicknameRequestDto;
import com.ssafy.algogo.user.dto.request.SignupRequestDto;
import com.ssafy.algogo.user.dto.request.UpdateUserInfoRequestDto;
import com.ssafy.algogo.user.dto.response.*;

public interface UserService {

    SignupResponseDto signup(SignupRequestDto dto);

    CheckDuplicateEmailResponseDto isAvailableEmail(CheckDuplicateEmailRequestDto checkDuplicateEmailRequestDto);

    CheckDuplicateNicknameResponseDto isAvailableNickname(CheckDuplicateNicknameRequestDto checkDuplicateNicknameRequestDto);

    UserInfoResponseDto getOneUserInfo(Long userId);

    UpdateUserInfoResponseDto updateUserInfo(Long userId, UpdateUserInfoRequestDto updateUserInfoRequestDto);

}
