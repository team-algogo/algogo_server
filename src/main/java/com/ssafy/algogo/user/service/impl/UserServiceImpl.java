package com.ssafy.algogo.user.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.user.dto.request.CheckDuplicateEmailRequestDto;
import com.ssafy.algogo.user.dto.request.CheckDuplicateNicknameRequestDto;
import com.ssafy.algogo.user.dto.request.SignupRequestDto;
import com.ssafy.algogo.user.dto.response.CheckDuplicateEmailResponseDto;
import com.ssafy.algogo.user.dto.response.CheckDuplicateNicknameResponseDto;
import com.ssafy.algogo.user.dto.response.SignupResponseDto;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.entity.UserRole;
import com.ssafy.algogo.user.repository.UserRepository;
import com.ssafy.algogo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public SignupResponseDto signup(SignupRequestDto dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException("이미 존재하는 이메일입니다.", ErrorCode.BAD_REQUEST); // TODO: 추후 커스텀 익셉션으로 변경
        }

        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new CustomException("이미 존재하는 닉네임입니다.", ErrorCode.BAD_REQUEST); // TODO: 위와 동일
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword()); // 직렬 암호화,

        User.UserBuilder userBuilder = User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .nickname(dto.getNickname())
                .userRole(UserRole.USER);

        User user = userBuilder.build();
        userRepository.save(user);

        return SignupResponseDto.response(user);
    }

    @Override
    public CheckDuplicateEmailResponseDto isAvailableEmail(CheckDuplicateEmailRequestDto dto) {

        boolean result = userRepository.existsByEmail(dto.getEmail());
        CheckDuplicateEmailResponseDto responseDto = null;

        if (result) {
            responseDto = new CheckDuplicateEmailResponseDto(false);
        } else {
            responseDto = new CheckDuplicateEmailResponseDto(true);
        }

        return responseDto;
    }

    @Override
    public CheckDuplicateNicknameResponseDto isAvailableNickname(CheckDuplicateNicknameRequestDto dto) {
        boolean result = userRepository.existsByNickname(dto.getNickname());
        CheckDuplicateNicknameResponseDto responseDto = null;

        if (result) {
            responseDto = new CheckDuplicateNicknameResponseDto(false);
        } else {
            responseDto = new CheckDuplicateNicknameResponseDto(true);
        }

        return responseDto;
    }

}
