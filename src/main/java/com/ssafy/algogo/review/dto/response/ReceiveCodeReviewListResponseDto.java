package com.ssafy.algogo.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReceiveCodeReviewListResponseDto(List<ReceiveCodeReviewResponseDto> receiveCodeReviews) {

  public ReceiveCodeReviewListResponseDto from(List<ReceiveCodeReviewResponseDto> receiveCodeReviews) {
    return new ReceiveCodeReviewListResponseDto(receiveCodeReviews);
  }
}
