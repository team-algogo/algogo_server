package com.ssafy.algogo.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReceiveCodeReviewResponseDto(
    Long submissionId,
    String problemTitle,
    String programType,
    String programTitle,
    String nickname,
    String content,
    LocalDateTime modifiedAt
) {

}
