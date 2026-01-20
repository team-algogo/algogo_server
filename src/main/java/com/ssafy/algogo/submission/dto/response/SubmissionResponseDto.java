package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.entity.Submission;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SubmissionResponseDto(Long submissionId, Long programProblemId, Long userId,
                                    String language, String code, String strategy, Long execTime,
                                    Long memory, Boolean isSuccess, Long viewCount, boolean isOwner,
                                    LocalDateTime createAt,
                                    LocalDateTime modifiedAt,
                                    List<AlgorithmResponseDto> algorithmList,
                                    BigDecimal aiScore,
                                    String aiScoreReason) {

    public static SubmissionResponseDto from(Submission submission,
        List<Algorithm> usedAlgorithmList, boolean isOwner) {
        return new SubmissionResponseDto(submission.getId(), submission.getProgramProblem().getId(),
            submission.getUser().getId(), submission.getLanguage(), submission.getCode(),
            submission.getStrategy(), submission.getExecTime(), submission.getMemory(),
            submission.getIsSuccess(), submission.getViewCount(), isOwner, submission.getCreatedAt(),
            submission.getModifiedAt(),
            usedAlgorithmList.stream().map(AlgorithmResponseDto::from).toList(),
            submission.getAiScore(),
            submission.getAiScoreReason());
    }
}
