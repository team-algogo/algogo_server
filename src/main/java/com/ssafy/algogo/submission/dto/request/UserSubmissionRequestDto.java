package com.ssafy.algogo.submission.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSubmissionRequestDto {

    private String nickname;
    private String language;
    private Boolean isSuccess;
    private String programType;
    private String algorithm;
    private String platform;
}
