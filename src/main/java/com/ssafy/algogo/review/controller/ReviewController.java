package com.ssafy.algogo.review.controller;

import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.review.dto.CodeReviewCreateRequestDto;
import com.ssafy.algogo.review.dto.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.service.ReviewService;
import jakarta.servlet.http.HttpServlet;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping("")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponse CodeReviewCreate(
      //@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody @Valid CodeReviewCreateRequestDto reviewRequest) {

    log.info("reviewRequest {}", reviewRequest);

    CodeReviewTreeResponseDto reviewResponse = reviewService.codeReviewCreate(reviewRequest, 1L);

    return new SuccessResponse("리뷰 작성을 성공했습니다.", reviewResponse);
  }


}
