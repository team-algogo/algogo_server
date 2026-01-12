package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface ProgramRepository extends JpaRepository<Program, Long> {

	boolean existsByTitle(String title);

	List<Program> findByProgramType(ProgramType programType);

	@Query("select pt.name from Program p join p.programType pt where p.id = :programId" )
	Optional<String> getProgramTypeByProgramId(Long programId);
}
