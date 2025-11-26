package com.ssafy.algogo.user.controller;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.user.dto.request.CheckDuplicateEmailRequestDto;
import com.ssafy.algogo.user.dto.request.CheckDuplicateNicknameRequestDto;
import com.ssafy.algogo.user.dto.request.SignupRequestDto;
import com.ssafy.algogo.user.dto.request.UpdateUserInfoRequestDto;
import com.ssafy.algogo.user.dto.response.*;
import com.ssafy.algogo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponse signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        SignupResponseDto signupResponseDto = userService.signup(signupRequestDto);
        return SuccessResponse.success("회원가입에 성공했습니다.", signupResponseDto);
    }

    @PostMapping("/check/emails")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse checkDuplicateEmail(@RequestBody @Valid CheckDuplicateEmailRequestDto checkDuplicateEmailRequestDto) {
        CheckDuplicateEmailResponseDto checkDuplicateEmailResponseDto = userService.isAvailableEmail(checkDuplicateEmailRequestDto);
        if (checkDuplicateEmailResponseDto.isAvailable()) {
            return SuccessResponse.success("사용가능한 이메일입니다.", checkDuplicateEmailResponseDto);
        } else {
            return SuccessResponse.success("이미 존재하는 이메일입니다.", checkDuplicateEmailResponseDto);
        }
    }

    @PostMapping("/check/nicknames")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse checkDuplicateNickname(@RequestBody @Valid CheckDuplicateNicknameRequestDto checkDuplicateNicknameRequestDto) {
        CheckDuplicateNicknameResponseDto checkDuplicateNicknameResponseDto = userService.isAvailableNickname(checkDuplicateNicknameRequestDto);
        if (checkDuplicateNicknameResponseDto.isAvailable()) {
            return SuccessResponse.success("사용가능한 닉네임입니다.", checkDuplicateNicknameResponseDto);
        } else {
            return SuccessResponse.success("이미 존재하는 닉네임입니다.", checkDuplicateNicknameResponseDto);
        }
    }

    @GetMapping("/profiles")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = (customUserDetails != null) ? customUserDetails.getUserId() : null;
        UserInfoResponseDto userInfoResponseDto = userService.getOneUserInfo(userId);
        return SuccessResponse.success("유저 정보 조회에 성공했습니다.", userInfoResponseDto);
    }

    @PutMapping("/profiles")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UpdateUserInfoRequestDto updateUserInfoRequestDto) {
        Long userId = (customUserDetails != null) ? customUserDetails.getUserId() : null;
        UpdateUserInfoResponseDto updateUserInfoResponseDto = userService.updateUserInfo(userId, updateUserInfoRequestDto);
        return SuccessResponse.success("사용자 정보 수정에 성공했습니다.", updateUserInfoResponseDto);
    }

}
