package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.InviteStatus;
import com.ssafy.algogo.program.entity.ProgramInvite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface ProgramInviteRepository extends JpaRepository<ProgramInvite, Long> {

    Optional<ProgramInvite> findByProgramIdAndUserIdAndInviteStatus(Long programId, Long userId, InviteStatus inviteStatus);

    @Query("SELECT pi FROM ProgramInvite pi " +
        "JOIN FETCH pi.user u " +
        "WHERE pi.program.id = :programId")
    List<ProgramInvite> findByProgramIdWithUser(Long programId);
}
