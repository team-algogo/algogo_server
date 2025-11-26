package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.JoinStatus;
import com.ssafy.algogo.program.entity.ProgramJoin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProgramJoinRepository extends JpaRepository<ProgramJoin, Long> {
  public Optional<ProgramJoin> findByUserIdAndProgramIdAndJoinStatus(Long userId, Long programId, JoinStatus joinStatus);
}
