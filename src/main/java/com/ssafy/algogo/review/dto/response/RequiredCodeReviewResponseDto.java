package com.ssafy.algogo.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequiredCodeReviewResponseDto(
    Long submissionId,
    String problemTitle,
    String programType,
    String programTitle,
    String nickname
) {

}
