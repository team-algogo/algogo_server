package com.ssafy.algogo.problem.repository.query;

import com.ssafy.algogo.problem.dto.response.ProgramProblemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProgramProblemRepositoryCustom {

	Page<ProgramProblemResponseDto> findAllByProgramIdWithSort(
		Long programId,
		String sortBy,
		String sortDirection,
		Pageable pageable
	);
}