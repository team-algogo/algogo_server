package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProgramCategoryRepository extends JpaRepository<ProgramCategory, Long> {

}
