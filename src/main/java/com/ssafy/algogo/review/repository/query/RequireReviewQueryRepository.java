package com.ssafy.algogo.review.repository.query;

import com.ssafy.algogo.review.dto.response.RequiredCodeReviewResponseDto;
import java.util.List;

public interface RequireReviewQueryRepository {

    List<RequiredCodeReviewResponseDto> getRequiredReviews(Long userId);
}