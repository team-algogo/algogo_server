package com.ssafy.algogo.submission.service;

import com.ssafy.algogo.submission.dto.request.SubmissionRequestDto;
import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.SubmissionAuthorActiveResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionAuthorStatusResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionListResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionMePageResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionStatsInfosResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionStatsPageResponseDto;
import com.ssafy.algogo.submission.dto.response.TrendIdsResponseDto;
import org.springframework.data.domain.Pageable;

public interface SubmissionService {

    SubmissionResponseDto getSubmission(Long userId, Long submissionId);

    SubmissionResponseDto createSubmission(Long userId, SubmissionRequestDto submissionRequestDto);

    void deleteSubmission(Long userId, Long submissionId);

    SubmissionListResponseDto getSubmissionHistories(Long userId, Long submissionId);

    SubmissionMePageResponseDto getSubmissionMe(Long userId,
        UserSubmissionRequestDto userSubmissionRequestDto, Pageable pageable);

    TrendIdsResponseDto getTrendIds(String trendType);

    SubmissionAuthorActiveResponseDto getSubmissionAuthorActive(Long submissionId);

    SubmissionStatsPageResponseDto getSubmissionStatsLists(Long userId,
        Long programProblemId,
        UserSubmissionRequestDto userSubmissionRequestDto, Pageable pageable);

    SubmissionStatsInfosResponseDto getSubmissionStatsInfos(Long userId, Long programProblemId);

    SubmissionAuthorStatusResponseDto canUserMoreSubmission(Long userId, Long programId);

    void retryAiEvaluation(Long userId, Long submissionId);
}
