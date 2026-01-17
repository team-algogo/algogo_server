package com.ssafy.algogo.review.controller;

import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.review.dto.request.CreateCodeReviewRequestDto;
import com.ssafy.algogo.review.dto.request.UpdateCodeReiewRequestDto;
import com.ssafy.algogo.review.dto.response.CodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.CodeReviewTreeResponseDto;
import com.ssafy.algogo.review.dto.response.CodeReviewResponseDto;
import com.ssafy.algogo.review.dto.response.UserCodeReviewListResponseDto;
import com.ssafy.algogo.review.dto.response.RequiredCodeReviewListResponseDto;
import com.ssafy.algogo.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

        CodeReviewResponseDto codeReviewResponseDto = reviewService.createCodeReview(
            reviewRequest,
            customUserDetails.getUserId());

        return new SuccessResponse("리뷰 작성을 성공했습니다.", codeReviewResponseDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("")
    public SuccessResponse getCodeReviews(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestParam("submission_id") Long submissionId) {

        CodeReviewListResponseDto codeReviewListResponseDto = reviewService.getReviewsBySubmissionId(
            customUserDetails.getUserId(),
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

        CodeReviewResponseDto codeReviewResponseDto = reviewService.editCodeReview(
            customUserDetails.getUserId(), reviewId, reviewRequest);

        return new SuccessResponse("리뷰 수정을 성공했습니다.", codeReviewResponseDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{reviewId}")
    public SuccessResponse deleteCodeReview(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long reviewId
    ) {

        reviewService.deleteCodeReview(customUserDetails.getUserId(), reviewId);

        return new SuccessResponse("리뷰 삭제를 성공했습니다.", null);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{reviewId}/likes")
    public SuccessResponse addCodeReviewLike(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long reviewId
    ) {

        Boolean createLike = reviewService.addCodeReviewLike(customUserDetails.getUserId(),
            reviewId);

        String message = createLike ? "리뷰 좋아요를 성공했습니다." : "이미 좋아요가 반영된 상태입니다.";

        return new SuccessResponse(message, null);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{reviewId}/likes")
    public SuccessResponse deleteCodeReviewLike(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long reviewId
    ) {

        Boolean createLike = reviewService.deleteCodeReviewLike(customUserDetails.getUserId(),
            reviewId);

        String message = createLike ? "리뷰 좋아요 취소를 성공했습니다." : "이미 좋아요 취소가 반영된 상태입니다.";

        return new SuccessResponse(message, null);
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

        UserCodeReviewListResponseDto userCodeReviewListResponseDto = reviewService.getReceiveReviews(
            customUserDetails.getUserId(), page, size);

        return new SuccessResponse("내가 받은 리뷰 리스트 조회를 성공했습니다.", userCodeReviewListResponseDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/lists/done")
    public SuccessResponse getDoneCodeReviewResponseDto(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer size

    ) {

        UserCodeReviewListResponseDto userCodeReviewListResponseDto = reviewService.getDoneReviews(
            customUserDetails.getUserId(), page, size);

        return new SuccessResponse("내가 한 리뷰 리스트 조회를 성공했습니다.", userCodeReviewListResponseDto);
    }


}