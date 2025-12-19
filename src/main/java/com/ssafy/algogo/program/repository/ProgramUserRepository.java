package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProgramUserRepository extends JpaRepository<ProgramUser, Long> {

    Optional<ProgramUser> findByUserIdAndProgramIdAndProgramUserStatus(Long userId, Long programId,
        ProgramUserStatus programUserStatus);

    // 특정 유저가 참여한 프로그램들 전부
    List<ProgramUser> findAllByUserId(Long userId);

    Optional<ProgramUser> findByUserIdAndProgramId(Long userId, Long programId);
}
