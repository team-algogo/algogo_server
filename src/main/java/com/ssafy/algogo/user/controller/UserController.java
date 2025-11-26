package com.ssafy.algogo.user.controller;

import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.user.dto.request.CheckDuplicateEmailRequestDto;
import com.ssafy.algogo.user.dto.request.CheckDuplicateNicknameRequestDto;
import com.ssafy.algogo.user.dto.request.SignupRequestDto;
import com.ssafy.algogo.user.dto.response.CheckDuplicateEmailResponseDto;
import com.ssafy.algogo.user.dto.response.CheckDuplicateNicknameResponseDto;
import com.ssafy.algogo.user.dto.response.SignupResponseDto;
import com.ssafy.algogo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponse signup(@RequestBody @Valid SignupRequestDto dto) {
        SignupResponseDto responseDto = userService.signup(dto);
        return SuccessResponse.success("회원가입에 성공했습니다.", responseDto);
    }

    @PostMapping("/check/emails")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse checkDuplicateEmail(@RequestBody @Valid CheckDuplicateEmailRequestDto dto) {
        CheckDuplicateEmailResponseDto responseDto = userService.isAvailableEmail(dto);
        if (responseDto.isAvailable()) {
            return SuccessResponse.success("사용가능한 이메일입니다.", responseDto);
        } else {
            return SuccessResponse.success("이미 존재하는 이메일입니다.", responseDto);
        }
    }

    @PostMapping("/check/nicknames")
    @ResponseStatus(HttpStatus.OK)
    public SuccessResponse checkDuplicateNickname(@RequestBody @Valid CheckDuplicateNicknameRequestDto dto) {
        CheckDuplicateNicknameResponseDto responseDto = userService.isAvailableNickname(dto);
        if (responseDto.isAvailable()) {
            return SuccessResponse.success("사용가능한 닉네임입니다.", responseDto);
        } else {
            return SuccessResponse.success("이미 존재하는 닉네임입니다.", responseDto);
        }
    }

}
