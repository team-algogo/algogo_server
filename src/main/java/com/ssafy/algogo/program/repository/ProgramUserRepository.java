package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProgramUserRepository extends JpaRepository<ProgramUser, Long> {

    Optional<ProgramUser> findByUserIdAndProgramIdAndProgramUserStatus(Long userId, Long programId,
        ProgramUserStatus programUserStatus);
}
