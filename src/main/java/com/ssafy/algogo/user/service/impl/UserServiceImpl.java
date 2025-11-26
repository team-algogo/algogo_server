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

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final S3Service s3Service;

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
        userRepository.save(user);

        return UpdateUserInfoResponseDto.from(user);
    }

    @Override
    public UpdateUserProfileImageResponseDto updateUserProfileImage(Long userId, MultipartFile image) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.", ErrorCode.USER_NOT_FOUND));

        String oldImageUrl = user.getProfileImage();
        // TODO : 이부분 만약 얻어왔는데 기본 이미지일경우에는 S3에 없긴해서 삭제는 안될듯 싶습니다,
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            s3Service.deleteImage(oldImageUrl);
        }

        String newImageUrl = s3Service.uploadProfileImage(image, userId);

        user.updateProfileImage(newImageUrl);
        // userRepository.save(user);
        //// ❌ Before
        //user.updateProfileImage(newImageUrl);
        //userRepository.save(user);  // 불필요
        //
        //// ✅ After
        //user.updateProfileImage(newImageUrl);  // 더티체킹으로 자동 저장
        //```
        //
        //**이유:** `@Transactional` 안에서 Entity를 변경하면 트랜잭션 커밋 시 자동으로 UPDATE 쿼리가 날아갑니다.
        //
        //---
        return UpdateUserProfileImageResponseDto.from(newImageUrl);
    }

}
