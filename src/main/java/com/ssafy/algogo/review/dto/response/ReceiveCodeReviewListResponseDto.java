package com.ssafy.algogo.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import java.util.List;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReceiveCodeReviewListResponseDto(
    PageInfo pageInfo,
    List<ReceiveCodeReviewResponseDto> receiveCodeReviews
) {

    public static ReceiveCodeReviewListResponseDto from(
        PageInfo pageInfo,
        List<ReceiveCodeReviewResponseDto> receiveCodeReviews
    ) {
        return new ReceiveCodeReviewListResponseDto(pageInfo, receiveCodeReviews);
    }
}