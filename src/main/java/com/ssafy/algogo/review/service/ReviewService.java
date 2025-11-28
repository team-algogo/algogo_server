package com.ssafy.algogo.review.service;

import com.ssafy.algogo.review.dto.request.CreateCodeReviewRequestDto;
import com.ssafy.algogo.review.dto.request.UpdateCodeReiewRequestDto;
import com.ssafy.algogo.review.dto.response.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.UserCodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewListResponseDto;

public interface ReviewService {

    CodeReviewTreeResponseDto createCodeReview(CreateCodeReviewRequestDto reviewRequest,
        Long userId);

    CodeReviewListResponseDto getReviewsBySubmissionId(Long submissionId);

    CodeReviewTreeResponseDto editCodeReview(Long userId, Long reviewId,
        UpdateCodeReiewRequestDto updateReview);

    void deleteCodeReview(Long userId, Long reviewId);

    Boolean addCodeReviewLike(Long userId, Long reviewId);

    Boolean deleteCodeReviewLike(Long userId, Long reviewId);

    RequiredCodeReviewListResponseDto getRequiredReviews(Long userId);

    UserCodeReviewListResponseDto getReceiveReviews(Long userId, Integer page,
        Integer size);

    UserCodeReviewListResponseDto getDoneReviews(Long userId, Integer page,
        Integer size);
}
