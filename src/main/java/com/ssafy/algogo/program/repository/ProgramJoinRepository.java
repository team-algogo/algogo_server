package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.JoinStatus;
import com.ssafy.algogo.program.entity.ProgramJoin;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface ProgramJoinRepository extends JpaRepository<ProgramJoin, Long> {

    @Query("SELECT pj FROM ProgramJoin pj " +
        "JOIN FETCH pj.user u " +
        "WHERE pj.program.id = :programId")
    List<ProgramJoin> findByProgramIdWithUser(Long programId);

    public Optional<ProgramJoin> findByUserIdAndProgramIdAndJoinStatus(Long userId, Long programId,
        JoinStatus joinStatus);
}
