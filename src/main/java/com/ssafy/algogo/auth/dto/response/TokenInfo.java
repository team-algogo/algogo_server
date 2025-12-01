package com.ssafy.algogo.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TokenInfo {
    private String accessToken;
    private String refreshToken;
}
