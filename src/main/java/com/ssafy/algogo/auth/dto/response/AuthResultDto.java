package com.ssafy.algogo.auth.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AuthResultDto {
    private LocalLoginResponseDto localLoginResponseDto;
    private TokenInfo tokenInfo;
}
