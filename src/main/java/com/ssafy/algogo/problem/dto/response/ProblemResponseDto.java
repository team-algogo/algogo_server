package com.ssafy.algogo.problem.dto.response;

import com.ssafy.algogo.problem.entity.DifficultyType;
import com.ssafy.algogo.problem.entity.PlatformType;
import com.ssafy.algogo.problem.entity.Problem;

public record ProblemResponseDto(
    Long id,
    PlatformType platformType,
    String problemNo,
    String title,
    DifficultyType difficultyType,
    String problemLink
) {

    public static ProblemResponseDto from(Problem problem) {
        return new ProblemResponseDto(
            problem.getId(),
            problem.getPlatformType(),
            problem.getProblemNo(),
            problem.getTitle(),
            problem.getDifficultyType(),
            problem.getProblemLink()
        );
    }
}
