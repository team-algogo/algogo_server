package com.ssafy.algogo.program.problemset.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemSetWithProgressResponseDto(
        Long programId,
        String title,
        String description,
        String thumbnail,
        LocalDateTime createAt,
        LocalDateTime modifiedAt,
        String programType,
        List<String> categories,
        Long totalParticipants,
        Long problemCount,
        Long solvedCount) {
}
