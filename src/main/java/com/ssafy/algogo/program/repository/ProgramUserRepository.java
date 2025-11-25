package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProgramUserRepository extends JpaRepository<ProgramUser, Long> {

}
