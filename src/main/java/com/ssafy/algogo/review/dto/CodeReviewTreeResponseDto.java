package com.ssafy.algogo.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
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

}
