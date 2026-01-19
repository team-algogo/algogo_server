package com.ssafy.algogo.user.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CheckEmailCodeRequestDto {

    private String email;
    private String code;
}
