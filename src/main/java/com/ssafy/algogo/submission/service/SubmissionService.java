package com.ssafy.algogo.submission.service;

import com.ssafy.algogo.submission.dto.request.SubmissionRequestDto;
import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.SubmissionAuthorActiveResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionListResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.dto.response.TrendIdsResponseDto;
import com.ssafy.algogo.submission.dto.response.UserSubmissionPageResponseDto;
import org.springframework.data.domain.Pageable;

public interface SubmissionService {

    SubmissionResponseDto getSubmission(Long submissionId);

    SubmissionResponseDto createSubmission(Long userId, SubmissionRequestDto submissionRequestDto);

    void deleteSubmission(Long userId, Long submissionId);

    SubmissionListResponseDto getSubmissionHistories(Long userId, Long submissionId);

    UserSubmissionPageResponseDto getSubmissionMe(Long userId,
        UserSubmissionRequestDto userSubmissionRequestDto, Pageable pageable);

    TrendIdsResponseDto getTrendIds(String trendType);

    SubmissionAuthorActiveResponseDto getSubmissionAuthorActive(Long submissionId);

    UserSubmissionPageResponseDto getSubmissionsByProgramProblem(Long userId, Long programProblemId,
        UserSubmissionRequestDto userSubmissionRequestDto, Pageable pageable);

}
