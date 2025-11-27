package com.ssafy.algogo.program.group.repository;

import com.ssafy.algogo.program.group.entity.GroupsUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface GroupUserRepository extends JpaRepository<GroupsUser, Long> {
  Optional<GroupsUser> findByProgramIdAndUserId(Long programId, Long userId);

  @Query("SELECT gu FROM GroupsUser gu " +
      "JOIN FETCH gu.user u " +
      "WHERE gu.program.id = :programId")
  List<GroupsUser> findByProgramIdWithUser(Long programId);
}
