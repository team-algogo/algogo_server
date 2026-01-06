package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.user.dto.response.UserSimpleResponseDto;

public record SubmissionStatsResponseDto(
    UserSimpleResponseDto userSimpleResponseDto,
    SubmissionResponseDto submissionResponseDto,
    Long reviewCount
) {

}
