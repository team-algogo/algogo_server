package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramInviteRepository extends JpaRepository<ProgramInvite, Long> {

}
