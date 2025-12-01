package com.ssafy.algogo.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class SignupRequestDto {

    // 이메일: @Email 어노테이션만 써도 되지만, 더 엄격한 검증이 필요하면 정규식 추가
    // (실무에서는 보통 @Email 하나로 퉁치거나, 인증 메일 발송으로 검증을 대신함)
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$",
            message = "Please enter a valid email format.")
    private String email;

    // 비밀번호: 영문, 숫자, 특수문자 포함 8자 이상 20자 이하 (가장 표준적인 보안 정책)
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "Password must contain at least one letter, one number, and one special character, and be between 8 and 20 characters.")
    private String password;

    // 닉네임: 한글, 영문, 숫자만 가능 (특수문자 금지), 2~10자
    // 실무에선 닉네임에 공백이나 이모지, 특수문자를 못 넣게 막는 경우가 많음
    @NotBlank(message = "Nickname is required")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
            message = "Nickname must be 2 to 10 characters long and contain only Korean, English, or numbers.")
    private String nickname;

}