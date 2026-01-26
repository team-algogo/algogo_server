package com.ssafy.algogo.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.problem.entity.PlatformType;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequiredCodeReviewResponseDto(
    String problemTitle,
    PlatformType problemPlatform,
    String programType,
    String programTitle,
    CodeReviewSubmissionInfoDto submission,
    LocalDateTime subjectSubmissionCreatedAt
) {

}
