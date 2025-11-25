package com.ssafy.algogo.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.review.entity.Review;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CodeReviewTreeResponseDto(Long reviewId,
                                        Long parentReviewId,
                                        Long userId,
                                        Long submissionId,
                                        Long likeCount,
                                        Long codeLine,
                                        String content,
                                        LocalDateTime createdAt,
                                        LocalDateTime modifiedAt,
                                        List<CodeReviewTreeResponseDto> children
                                        ) {
  public static CodeReviewTreeResponseDto from(Review review) {

    Review parent = review.getParentReview();
    Long parentReviewId = parent != null ? parent.getId() : null;

    return new CodeReviewTreeResponseDto(
        review.getId(),
        parentReviewId,
        review.getUser().getId(),
        review.getSubmission().getId(),
        review.getLikeCount(),
        review.getCodeLine(),
        review.getContent(),
        review.getCreatedAt(),
        review.getModifiedAt(),
        new ArrayList<>()
    );
  }

}
