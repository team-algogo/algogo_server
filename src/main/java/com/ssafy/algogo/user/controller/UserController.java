package com.ssafy.algogo.user.controller;

import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.user.dto.request.SignupRequestDto;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.events.DTD;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponse signup(@RequestBody @Valid SignupRequestDto dto) {
        User user = userService.signup(dto);
        return SuccessResponse.success("회원가입에 성공했습니다.", user);
    }

}
