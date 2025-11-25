package com.ssafy.algogo.review.service;

import com.ssafy.algogo.review.dto.request.CodeReviewCreateRequestDto;
import com.ssafy.algogo.review.dto.response.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewListResponseDto;

public interface ReviewService {

  public CodeReviewTreeResponseDto codeReviewCreate(CodeReviewCreateRequestDto reviewRequest, Long userId);
  public CodeReviewListResponseDto getReviewsBySubmissionId(Long submissionId);
  public RequiredCodeReviewListResponseDto getRequiredReviews(Long userId);
}
