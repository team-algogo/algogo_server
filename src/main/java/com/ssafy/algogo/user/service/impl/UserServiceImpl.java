package com.ssafy.algogo.user.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.common.utils.S3Service;
import com.ssafy.algogo.user.dto.request.*;
import com.ssafy.algogo.user.dto.response.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_USER_IMAGE = "https://d3ud9ocg2cusae.cloudfront.net/files/8/0f3b175d-1d3f-4b70-8438-5466f154a0b6.png";
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    public SignupResponseDto signup(SignupRequestDto dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException("이미 존재하는 이메일입니다.", ErrorCode.ALREADY_EXISTS_EMAIL);
        }

        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new CustomException("이미 존재하는 닉네임입니다.", ErrorCode.ALREADY_EXISTS_NICKNAME);
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword()); // 직렬 암호화,

        User.UserBuilder userBuilder = User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .nickname(dto.getNickname())
                .userRole(UserRole.USER);

        User user = userBuilder.build();
        userRepository.save(user); // -> 얘는 더티체킹이 아니라 신규 객체기때문에 save를 해야한다, 수정은 기존 데이터를 이미 JPA가 알고있기에 감시하고 더티체킹,

        return SignupResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponseDto getOneUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.", ErrorCode.USER_NOT_FOUND));

        return UserInfoResponseDto.from(user);
    }

    @Override
    public UpdateUserInfoResponseDto updateUserInfo(Long userId, UpdateUserInfoRequestDto updateUserInfoRequestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.", ErrorCode.USER_NOT_FOUND));

        // TODO : 닉네임 수정시, 화면에서도 중복 체크 버튼을 해줘야한다. 이 부분은 아직 말을 안한 거 같다. -> 에러로 처리하진 않겠다. 중복체크는 에러가 아니다.

        user.updateUserInfo(updateUserInfoRequestDto.getNickname(), updateUserInfoRequestDto.getDescription());

        return UpdateUserInfoResponseDto.from(user);
    }

    @Override
    public UpdateUserProfileImageResponseDto updateUserProfileImage(Long userId, MultipartFile image) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.", ErrorCode.USER_NOT_FOUND));

        String newImageUrl = s3Service.uploadProfileImage(image, userId);

        user.updateProfileImage(newImageUrl);
        // userRepository.save(user); -> 이 부분을 하지 않아도 된다. 배웠습니다..
        //**이유:** `@Transactional` 안에서 Entity를 변경하면 트랜잭션 커밋 시 자동으로 UPDATE 쿼리가 날아갑니다.

        return UpdateUserProfileImageResponseDto.from(newImageUrl);
    }

    @Override
    public UpdateUserProfileImageResponseDto updateDefaultProfileImage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.", ErrorCode.USER_NOT_FOUND));

        user.updateProfileImage(DEFAULT_USER_IMAGE);
        return UpdateUserProfileImageResponseDto.from(DEFAULT_USER_IMAGE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchUserResponseDto> searchUserListByContent(String content) {
        // IgnoreCase -> 대소문자 구별 X 모두 나타냄
        return userRepository.findByEmailContainingIgnoreCase(content).stream()
                .map(SearchUserResponseDto::from)
                .toList();
    }

}
