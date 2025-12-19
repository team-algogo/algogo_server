package com.ssafy.algogo.problem.dto.response;

import com.ssafy.algogo.problem.entity.DifficultyType;
import com.ssafy.algogo.problem.entity.DifficultyViewType;
import com.ssafy.algogo.problem.entity.PlatformType;
import com.ssafy.algogo.problem.entity.Problem;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.entity.UserDifficultyType;

public record ProblemDetailResponseDto(
    Long id,
    PlatformType platformType,
    String problemNo,
    String title,
    DifficultyType difficultyType,
    String problemLink,
    UserDifficultyType userDifficultyType,
    DifficultyViewType difficultyViewType
) {

    public static ProblemDetailResponseDto from(ProgramProblem pp) {
        Problem p = pp.getProblem();
        return new ProblemDetailResponseDto(
            p.getId(),
            p.getPlatformType(),
            p.getProblemNo(),
            p.getTitle(),
            p.getDifficultyType(),
            p.getProblemLink(),
            pp.getUserDifficultyType(),
            pp.getDifficultyViewType()
        );
    }
}