package com.ssafy.algogo.program.group.repository;

import com.ssafy.algogo.program.group.entity.GroupsUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface GroupUserRepository extends JpaRepository<GroupsUser, Long> {

    Optional<GroupsUser> findByProgramIdAndUserId(Long programId, Long userId);

    @Query("SELECT gu FROM GroupsUser gu " +
        "JOIN FETCH gu.user u " +
        "WHERE gu.program.id = :programId " +
        "AND gu.programUserStatus = :programUserStatus")
    List<GroupsUser> findByProgramIdAndProgramUserStatusWithUser(Long programId,
        ProgramUserStatus programUserStatus);

    Optional<GroupsUser> findByProgramIdAndUserIdAndProgramUserStatus(Long programId,
        Long programUserId, ProgramUserStatus programUserStatus);

    @Query("SELECT gu FROM GroupsUser gu " +
        "JOIN FETCH gu.user u " +
        "WHERE gu.program.id = :programId " +
        "AND gu.user.id = :userId")
    Optional<GroupsUser> findByProgramIdAndUserIdWithUser(Long programId, Long userId);
}
