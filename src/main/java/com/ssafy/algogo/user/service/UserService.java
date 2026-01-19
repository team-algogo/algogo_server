package com.ssafy.algogo.user.service;


import com.ssafy.algogo.user.dto.request.*;
import com.ssafy.algogo.user.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    SignupResponseDto signup(SignupRequestDto dto);

    CheckDuplicateEmailResponseDto isAvailableEmail(CheckDuplicateEmailRequestDto checkDuplicateEmailRequestDto);

    CheckDuplicateNicknameResponseDto isAvailableNickname(CheckDuplicateNicknameRequestDto checkDuplicateNicknameRequestDto);

    UserInfoResponseDto getOneUserInfo(Long userId);

    UpdateUserInfoResponseDto updateUserInfo(Long userId, UpdateUserInfoRequestDto updateUserInfoRequestDto);

    UpdateUserProfileImageResponseDto updateUserProfileImage(Long userId, MultipartFile image);

    UpdateUserProfileImageResponseDto updateDefaultProfileImage(Long userId);

    ListSearchUserResponseDto searchUserListByContent(String content);

    void sendToEmail(SendEmailCodeRequestDto sendEmailCodeRequestDto);

    void verifiedCode(CheckEmailCodeRequestDto checkEmailCodeRequestDto);
}
