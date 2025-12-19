package com.ssafy.algogo.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.common.dto.PageInfo;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserCodeReviewListResponseDto(
    PageInfo pageInfo,
    List<UserCodeReviewResponseDto> receiveCodeReviews
) {

    public static UserCodeReviewListResponseDto from(
        PageInfo pageInfo,
        List<UserCodeReviewResponseDto> receiveCodeReviews
    ) {
        return new UserCodeReviewListResponseDto(pageInfo, receiveCodeReviews);
    }
}