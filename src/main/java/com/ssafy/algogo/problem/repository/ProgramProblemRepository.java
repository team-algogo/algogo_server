package com.ssafy.algogo.problem.repository;

import com.ssafy.algogo.problem.entity.ProgramProblem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramProblemRepository extends JpaRepository<ProgramProblem, Long> {
}
