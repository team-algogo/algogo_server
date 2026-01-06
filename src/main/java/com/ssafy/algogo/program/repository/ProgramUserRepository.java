package com.ssafy.algogo.program.repository;

import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface ProgramUserRepository extends JpaRepository<ProgramUser, Long> {

	Optional<ProgramUser> findByUserIdAndProgramIdAndProgramUserStatus(Long userId, Long programId,
		ProgramUserStatus programUserStatus);


	Optional<ProgramUser> findByUserIdAndProgramId(Long userId, Long programId);

	@Query("SELECT DISTINCT pu.program.id FROM ProgramUser pu " +
		"WHERE pu.user.id = :userId " +
		"AND pu.program.programType.id = 2 " +
		"AND pu.programUserStatus = 'ACTIVE'")
	List<Long> findActiveProblemSetIdsByUserId(@Param("userId") Long userId);
}
