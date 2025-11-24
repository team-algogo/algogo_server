package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramJoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramJoinRepository extends JpaRepository<ProgramJoin, Long> {

}
