package com.ssafy.algogo.problem.repository;

import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.query.ProgramProblemRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramProblemRepository extends JpaRepository<ProgramProblem, Long>,
	ProgramProblemRepositoryCustom {

	Page<ProgramProblem> findAllByProgramId(Long programId, Pageable pageable);
}
