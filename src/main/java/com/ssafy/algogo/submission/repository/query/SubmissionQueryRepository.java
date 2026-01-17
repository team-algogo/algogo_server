package com.ssafy.algogo.submission.repository.query;

import com.ssafy.algogo.submission.dto.ReviewCandidateQueryDto;
import com.ssafy.algogo.submission.dto.request.UserSubmissionRequestDto;
import com.ssafy.algogo.submission.dto.response.SubmissionMeResponseDto;
import com.ssafy.algogo.submission.dto.response.SubmissionStatsResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubmissionQueryRepository {

    Page<SubmissionMeResponseDto> findAllUserSubmissionList(Long userId,
        UserSubmissionRequestDto userSubmissionRequestDto, Pageable pageable);

    List<ReviewCandidateQueryDto> findReviewMatchCandidates(Long subjectSubmissionId,
        Long subjectUserId,
        Long programProblemId, String language);

    List<Long> findHotSubmissionIds();

    List<Long> findRecentSubmissionIds();

    List<Long> findTrendProgramProblemIds();

    Boolean isSubmissionAuthorActive(Long submissionId);

    Page<SubmissionStatsResponseDto> findAllSubmissionsByProgramProblem(Long programProblemId,
        UserSubmissionRequestDto userSubmissionRequestDto,
        Pageable pageable);

    boolean canUserMoreSubmit(Long userId, String programType, Long programId);
}
