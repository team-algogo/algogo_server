package com.ssafy.algogo.review.service;

import com.ssafy.algogo.review.dto.request.CreateCodeReviewRequestDto;
import com.ssafy.algogo.review.dto.request.UpdateCodeReiewRequestDto;
import com.ssafy.algogo.review.dto.response.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.UserCodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewListResponseDto;

public interface ReviewService {

    public CodeReviewTreeResponseDto createCodeReview(CreateCodeReviewRequestDto reviewRequest,
        Long userId);

    public CodeReviewListResponseDto getReviewsBySubmissionId(Long submissionId);

    public CodeReviewTreeResponseDto editCodeReview(Long userId, Long reviewId,
        UpdateCodeReiewRequestDto updateReview);

    public void deleteCodeReview(Long userId, Long reviewId);

    public RequiredCodeReviewListResponseDto getRequiredReviews(Long userId);

    public UserCodeReviewListResponseDto getReceiveReviews(Long userId, Integer page,
        Integer size);

    public UserCodeReviewListResponseDto getDoneReviews(Long userId, Integer page,
        Integer size);
}
