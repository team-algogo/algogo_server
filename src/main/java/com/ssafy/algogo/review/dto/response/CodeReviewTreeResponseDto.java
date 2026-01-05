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
                                        boolean isLike,
                                        List<CodeReviewTreeResponseDto> children
) {

    public CodeReviewTreeResponseDto(
        Long reviewId,
        Long parentReviewId,
        Long userId,
        Long submissionId,
        Long likeCount,
        Long codeLine,
        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        boolean isLike
    ) {
        this(
            reviewId,
            parentReviewId,
            userId,
            submissionId,
            likeCount,
            codeLine,
            content,
            createdAt,
            modifiedAt,
            isLike,
            new ArrayList<>()
        );
    }
}