package com.ssafy.algogo.review.service;

import com.ssafy.algogo.review.dto.CodeReviewCreateRequestDto;
import com.ssafy.algogo.review.dto.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.CodeReviewTreeResponseDto;

public interface ReviewService {

  public CodeReviewTreeResponseDto codeReviewCreate(CodeReviewCreateRequestDto reviewRequest, Long userId);

}
