package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.user.dto.response.UserSimpleResponseDto;

public record SubmissionStatsResponseDto(
    UserSimpleResponseDto user,
    SubmissionResponseDto submission,
    Long reviewCount
) {

}
