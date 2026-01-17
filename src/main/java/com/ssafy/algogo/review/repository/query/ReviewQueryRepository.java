package com.ssafy.algogo.review.repository.query;

import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.UserCodeReviewResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ReviewQueryRepository {

    List<CodeReviewTreeResponseDto> getReviewsBySubmissionId(Long userId, Long submissionId);

    Page<UserCodeReviewResponseDto> getReceiveReviews(Long userId, Integer page, Integer size);

    Page<UserCodeReviewResponseDto> getDoneReviews(Long userId, Integer page, Integer size);
}
