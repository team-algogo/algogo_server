package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramTypeRepository extends JpaRepository<ProgramType, Long> {

}
