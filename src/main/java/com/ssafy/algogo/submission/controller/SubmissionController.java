package com.ssafy.algogo.submission.controller;

import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.submission.dto.request.SubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.SubmissionListResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

  private final SubmissionService submissionService;

  @GetMapping("/{submissionId}")
  public SuccessResponse getSubmission(@PathVariable Long submissionId) {
    SubmissionResponseDto submissionResponseDto = submissionService.getSubmission(submissionId);
    return new SuccessResponse("제출 조회를 성공했습니다.", submissionResponseDto);
  }

  @PostMapping
  public SuccessResponse createSubmission(
//            @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody @Valid SubmissionRequestDto submissionRequestDto) {
    log.info("SubmissionRequestDto : {}", submissionRequestDto);
    SubmissionResponseDto submissionResponseDto = submissionService.createSubmission(12L,
        submissionRequestDto);

    return new SuccessResponse("코드 제출을 성공했습니다.", submissionResponseDto);
  }

  @GetMapping("/{submissionId}/histories")
  public SuccessResponse getSubmissionHistories(
//            @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long submissionId) {
    SubmissionListResponseDto submissionHistories = submissionService.getSubmissionHistories(1L,
        submissionId);
    return new SuccessResponse("제출 히스토리 조회를 성공했습니다.", submissionHistories);
  }

  @GetMapping("/{submissionId}/me")
  public SuccessResponse getSubmissionMe() {
    return null;
  }
}
