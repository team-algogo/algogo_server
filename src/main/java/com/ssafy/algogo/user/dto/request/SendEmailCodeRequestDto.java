package com.ssafy.algogo.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SendEmailCodeRequestDto {

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

}
