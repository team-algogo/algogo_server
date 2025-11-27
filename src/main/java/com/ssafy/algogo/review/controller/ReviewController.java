package com.ssafy.algogo.review.controller;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.review.dto.request.CreateCodeReviewRequestDto;
import com.ssafy.algogo.review.dto.request.UpdateCodeReiewRequestDto;
import com.ssafy.algogo.review.dto.response.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.ReceiveCodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.ReceiveCodeReviewResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewListResponseDto;
import com.ssafy.algogo.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public SuccessResponse createCodeReview(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestBody @Valid CreateCodeReviewRequestDto reviewRequest) {

        CodeReviewTreeResponseDto codeReviewTreeResponseDto = reviewService.createCodeReview(
            reviewRequest,
            customUserDetails.getUserId());

        return new SuccessResponse("리뷰 작성을 성공했습니다.", codeReviewTreeResponseDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("")
    public SuccessResponse getCodeReviews(
        @RequestParam("submission_id") Long submissionId) {

        CodeReviewListResponseDto codeReviewListResponseDto = reviewService.getReviewsBySubmissionId(
            submissionId);

        return new SuccessResponse("리뷰 작성내역 조회를 성공했습니다.", codeReviewListResponseDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{reviewId}")
    public SuccessResponse editCodeReview(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestBody @Valid UpdateCodeReiewRequestDto reviewRequest,
        @PathVariable Long reviewId
    ) {

        CodeReviewTreeResponseDto codeReviewTreeResponseDto = reviewService.editCodeReview(
            customUserDetails.getUserId(), reviewId, reviewRequest);

        return new SuccessResponse("리뷰 수정을 성공했습니다.", codeReviewTreeResponseDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/lists/require")
    public SuccessResponse getRequiredCodeReviewResponseDto(
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        RequiredCodeReviewListResponseDto requiredCodeReviewListResponseDto = reviewService.getRequiredReviews(
            customUserDetails.getUserId());

        return new SuccessResponse("내가 해야할 리뷰 리스트 조회를 성공했습니다.", requiredCodeReviewListResponseDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/lists/receive")
    public SuccessResponse getReceiveCodeReviewResponseDto(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer size

    ) {

        ReceiveCodeReviewListResponseDto receiveCodeReviewListResponseDto = reviewService.getReceiveReviews(
            customUserDetails.getUserId(), page, size);

        return new SuccessResponse("내가 받은 리뷰 리스트 조회를 성공했습니다.", receiveCodeReviewListResponseDto);
    }

}