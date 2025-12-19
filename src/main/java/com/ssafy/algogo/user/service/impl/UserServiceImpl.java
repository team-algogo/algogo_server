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

    private static final Long SEARCH_CONTENT_LIMIT = 20L;
    private static final String DEFAULT_USER_IMAGE = "https://d3ud9ocg2cusae.cloudfront.net/files/8/0f3b175d-1d3f-4b70-8438-5466f154a0b6.png";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {

        if (userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new CustomException("이미 존재하는 이메일입니다.", ErrorCode.ALREADY_EXISTS_EMAIL);
        }

        if (userRepository.existsByNickname(signupRequestDto.getNickname())) {
            throw new CustomException("이미 존재하는 닉네임입니다.", ErrorCode.ALREADY_EXISTS_NICKNAME);
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword()); // 직렬 암호화,

        User.UserBuilder userBuilder = User.builder()
                .email(signupRequestDto.getEmail())
                .password(encodedPassword)
                .nickname(signupRequestDto.getNickname())
                .userRole(UserRole.USER);

        User user = userBuilder.build();
        userRepository.save(user); // -> 얘는 더티체킹이 아니라 신규 객체기때문에 save를 해야한다, 수정은 기존 데이터를 이미 JPA가 알고있기에 감시하고 더티체킹,

        return SignupResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckDuplicateEmailResponseDto isAvailableEmail(CheckDuplicateEmailRequestDto checkDuplicateEmailRequestDto) {

        boolean result = userRepository.existsByEmail(checkDuplicateEmailRequestDto.getEmail());
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
    public CheckDuplicateNicknameResponseDto isAvailableNickname(CheckDuplicateNicknameRequestDto checkDuplicateNicknameRequestDto) {
        boolean result = userRepository.existsByNickname(checkDuplicateNicknameRequestDto.getNickname());
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

        if (userRepository.existsByNickname(updateUserInfoRequestDto.getNickname())) {
            throw new CustomException("이미 존재하는 닉네임입니다.", ErrorCode.ALREADY_EXISTS_NICKNAME);
        }

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
    public ListSearchUserResponseDto searchUserListByContent(String content) {

        if(content.isEmpty()) {
            throw new CustomException("빈값은 검색할 수 없습니다.", ErrorCode.SEARCH_EMPTY_CONTENT);
        }

        if (content.length() > SEARCH_CONTENT_LIMIT) {
            throw new CustomException(String.format("회원 검색 길이는 %d자 이상 검색할 수 없습니다.", SEARCH_CONTENT_LIMIT), ErrorCode.SEARCH_CONTENT_LENGTH_OVER);
        }

        if (!content.matches("^[a-zA-Z0-9가-힣@._-]+$")) {
            throw new CustomException("검색어에 특수문자를 포함할 수 없습니다.", ErrorCode.INVALID_PARAMETER);
        }

        // IgnoreCase -> 대소문자 구별 X 모두 나타냄
        List<SearchUserResponseDto> users = userRepository.findByEmailContainingIgnoreCase(content).stream()
                .map(SearchUserResponseDto::from)
                .toList();

        return new ListSearchUserResponseDto(users);
    }

}
