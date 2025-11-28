package com.ssafy.algogo.submission.dto.response;

import java.util.List;

public record SubmissionListResponseDto(
    List<SubmissionResponseDto> submissions
) {

  public SubmissionListResponseDto from(List<SubmissionResponseDto> submissions) {
    return new SubmissionListResponseDto(submissions);
  }
}
