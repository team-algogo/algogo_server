package com.ssafy.algogo.review.repository.query;

import com.ssafy.algogo.review.dto.response.UserCodeReviewResponseDto;
import org.springframework.data.domain.Page;

public interface ReviewQueryRepository {

    Page<UserCodeReviewResponseDto> getReceiveReviews(Long userId, Integer page, Integer size);

    Page<UserCodeReviewResponseDto> getDoneReviews(Long userId, Integer page, Integer size);
}
