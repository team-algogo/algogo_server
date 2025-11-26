package com.ssafy.algogo.problem.dto.response;

import com.ssafy.algogo.problem.entity.DifficultyViewType;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.entity.UserDifficultyType;

import java.time.LocalDateTime;

public record ProgramProblemResponseDto (
        Long programProblemId,
        Long participantCount,
        Long submissionCount,
        Long solvedCount,
        Long viewCount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        UserDifficultyType userDifficultyType,
        DifficultyViewType difficultyViewType,

        // ProgrmaProblem에 하위 리소스 개념이 커서 중첩구조로 가져가는게 좋아보임.
        ProblemResponseDto problemResponseDto
    ){
    public static ProgramProblemResponseDto from(ProgramProblem programProblem){
        return new ProgramProblemResponseDto(
                programProblem.getId(),
                programProblem.getParticipantCount(),
                programProblem.getSubmissionCount(),
                programProblem.getSolvedCount(),
                programProblem.getViewCount(),
                programProblem.getStartDate(),
                programProblem.getEndDate(),
                programProblem.getUserDifficultyType(),
                programProblem.getDifficultyViewType(),
                ProblemResponseDto.from(programProblem.getProblem())
        );
    }
}
