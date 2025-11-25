package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProgramRepository extends JpaRepository<Program, Long> {
  boolean existsByTitle(String title);
}
