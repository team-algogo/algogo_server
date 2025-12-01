package com.ssafy.algogo.program.problemset.dto.response;

import com.ssafy.algogo.common.dto.SortInfo;
import com.ssafy.algogo.problem.dto.response.ProgramProblemResponseDto;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.common.dto.PageInfo;

import java.util.List;
import org.springframework.data.domain.Page;

public record ProblemSetProblemsPageResponseDto(
	boolean isLogined,
	PageInfo page,
	SortInfo sort,
	List<ProgramProblemResponseDto> problemList
) {

	public static ProblemSetProblemsPageResponseDto of(
		boolean isLogined,
		Page<ProgramProblem> programProblems,
		String sortBy,
		String sortDirection
	) {
		List<ProgramProblemResponseDto> list =
			programProblems.map(ProgramProblemResponseDto::from).getContent();

		PageInfo pageInfo = new PageInfo(
			programProblems.getNumber(),
			programProblems.getSize(),
			programProblems.getTotalElements(),
			programProblems.getTotalPages()
		);

		SortInfo sortInfo = new SortInfo(sortBy, sortDirection);

		return new ProblemSetProblemsPageResponseDto(
			isLogined,
			pageInfo,
			sortInfo,
			list
		);
	}
}