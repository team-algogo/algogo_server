package com.ssafy.algogo.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CodeReviewListResponseDto(List<CodeReviewTreeResponseDto> reviews) {

}
