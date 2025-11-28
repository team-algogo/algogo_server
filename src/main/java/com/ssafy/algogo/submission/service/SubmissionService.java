package com.ssafy.algogo.submission.service;

import com.ssafy.algogo.submission.dto.request.SubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.SubmissionListResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;

public interface SubmissionService {

  SubmissionResponseDto getSubmission(Long submissionId);

  SubmissionResponseDto createSubmission(Long userId, SubmissionRequestDto submissionRequestDto);

  SubmissionListResponseDto getSubmissionHistories(Long userId, Long submissionId);
}
