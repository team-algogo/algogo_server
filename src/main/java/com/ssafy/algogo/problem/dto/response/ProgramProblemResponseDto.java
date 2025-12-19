package com.ssafy.algogo.problem.dto.response;

import com.ssafy.algogo.problem.entity.DifficultyViewType;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.entity.UserDifficultyType;
import java.time.LocalDateTime;

public record ProgramProblemResponseDto(
	Long programProblemId,
	Long participantCount,
	Long submissionCount,
	Long solvedCount,
	Long viewCount,
	LocalDateTime startDate,
	LocalDateTime endDate,
	UserDifficultyType userDifficultyType,
	DifficultyViewType difficultyViewType,
	Integer difficultyScore,
	Double accuracy,

	// ProgrmaProblem에 하위 리소스 개념이 커서 중첩구조로 가져가는게 좋아보임.
	ProblemResponseDto problemResponseDto
) {

	public static ProgramProblemResponseDto from(ProgramProblem programProblem) {
		// 정답률 계산 (제출이 0이면 null)
		Double accuracy = null;
		if (programProblem.getSubmissionCount() != null &&
			programProblem.getSubmissionCount() > 0) {
			accuracy = (programProblem.getSolvedCount() * 100.0)
				/ programProblem.getSubmissionCount();
		}
		Integer difficultyScore = null;
		if (programProblem.getProblem().getDifficultyType() != null) {
			difficultyScore = programProblem.getProblem()
				.getDifficultyType()
				.score();
		}

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
			difficultyScore,
			accuracy,
			ProblemResponseDto.from(programProblem.getProblem())
		);
	}
}
