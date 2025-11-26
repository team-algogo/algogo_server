package com.ssafy.algogo.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CreateCodeReviewRequestDto {
  @NotNull(message = "submissionId is required")
  private Long submissionId;

  private Long parentReviewId;

  private Long codeLine;

  @NotBlank(message = "content is required")
  private String content;

}
