package com.ssafy.algogo.review.repository.query;

import com.ssafy.algogo.review.dto.response.ReceiveCodeReviewResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ReviewQueryRepository {

    Page<ReceiveCodeReviewResponseDto> getReceiveReviews(Long userId, Integer page, Integer size);
}
