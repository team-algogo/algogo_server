package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface ProgramCategoryRepository extends JpaRepository<ProgramCategory, Long> {

	@Modifying
	@Query("DELETE FROM ProgramCategory pc WHERE pc.program.id = :programId")
	void deleteByProgramId(@Param("programId") Long programId);
}
