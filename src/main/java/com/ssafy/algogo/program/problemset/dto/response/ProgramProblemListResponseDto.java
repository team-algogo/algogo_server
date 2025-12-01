package com.ssafy.algogo.program.problemset.dto.response;

import com.ssafy.algogo.problem.dto.response.ProgramProblemResponseDto;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import java.util.List;
import org.springframework.data.domain.Page;

public record ProgramProblemListResponseDto(
	boolean isLogined,
	List<ProgramProblemResponseDto> problemList
) {

	public static ProgramProblemListResponseDto from(
		boolean isLogined,
		Page<ProgramProblem> programProblems
	) {
		return new ProgramProblemListResponseDto(
			isLogined,
			programProblems
				.map(ProgramProblemResponseDto::from)
				.toList()
		);
	}
}

