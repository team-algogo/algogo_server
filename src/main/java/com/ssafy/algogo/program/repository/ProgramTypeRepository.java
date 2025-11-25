package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProgramTypeRepository extends JpaRepository<ProgramType, Long> {
  Optional<ProgramType> findByName(String name);
}
