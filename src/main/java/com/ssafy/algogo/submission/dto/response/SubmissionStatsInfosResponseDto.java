package com.ssafy.algogo.submission.dto.response;

public record SubmissionStatsInfosResponseDto(
    Long submissionCount,
    Long successCount,
    Long failureCount,
    Integer successRate
) {

    public static SubmissionStatsInfosResponseDto from(
        Long submissionCount, Long successCount,
        Long failureCount, Integer successRate) {
        return new SubmissionStatsInfosResponseDto(
            submissionCount, successCount, failureCount, successRate);
    }
}
