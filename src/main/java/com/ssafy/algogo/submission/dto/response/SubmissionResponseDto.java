package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.submission.entity.Submission;
import java.time.LocalDateTime;
import java.util.List;

public record SubmissionResponseDto(Long submissionId, Long programProblemId, Long userId,
                                    String language, String code, String strategy, Long execTime,
                                    Long memory, Boolean isSuccess, LocalDateTime createAt,
                                    LocalDateTime modifiedAt,
                                    List<AlgorithmResponseDto> algorithm) {

  public static SubmissionResponseDto from(Submission submission) {
    return new SubmissionResponseDto(submission.getId(), submission.getProgramProblem().getId(),
        submission.getUser().getId(), submission.getLanguage(), submission.getCode(),
        submission.getStrategy(), submission.getExecTime(), submission.getMemory(),
        submission.getIsSuccess(), submission.getCreatedAt(), submission.getModifiedAt(), null);
  }
}
