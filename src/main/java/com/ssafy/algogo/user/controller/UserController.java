package com.ssafy.algogo.user.controller;

import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.user.dto.request.CheckDuplicateEmailRequestDto;
import com.ssafy.algogo.user.dto.request.SignupRequestDto;
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

        return null;
    }

}
