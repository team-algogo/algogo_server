package com.ssafy.algogo.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequiredCodeReviewListResponseDto(List<RequiredCodeReviewResponseDto> requiredCodeReviews) {

  public static RequiredCodeReviewListResponseDto from(List<RequiredCodeReviewResponseDto> requiredCodeReviews) {
      return new RequiredCodeReviewListResponseDto(requiredCodeReviews);
  }

}
