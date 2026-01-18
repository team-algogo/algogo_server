package com.ssafy.algogo.program.group.repository;

import com.ssafy.algogo.program.group.entity.GroupsUser;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface GroupUserRepository extends JpaRepository<GroupsUser, Long> {

    Optional<GroupsUser> findByProgramIdAndUserId(Long programId, Long userId);

    @Query("""
            SELECT gu
            FROM GroupsUser gu
            JOIN FETCH gu.user u
            WHERE gu.program.id = :programId
              AND gu.programUserStatus = :programUserStatus
        """)
    List<GroupsUser> findByProgramIdAndProgramUserStatusWithUser(Long programId,
        ProgramUserStatus programUserStatus);

    Optional<GroupsUser> findByProgramIdAndUserIdAndProgramUserStatus(Long programId,
        Long programUserId, ProgramUserStatus programUserStatus);

    @Query("""
            SELECT gu
            FROM GroupsUser gu
            JOIN FETCH gu.user u
            WHERE gu.program.id = :programId
              AND gu.user.id = :userId
        """)
    Optional<GroupsUser> findByProgramIdAndUserIdWithUser(Long programId, Long userId);

    @Query("""
            SELECT gu.program.id
            FROM GroupsUser gu
            WHERE gu.user.id = :userId
              AND gu.programUserStatus = 'ACTIVE'
        """)
    List<Long> findActiveProgramIdsByUserId(Long userId);

    @Query("""
        select gu.user
        from GroupsUser gu
        where gu.program.id = :programId
          and gu.programUserStatus = 'ACTIVE'
          and gu.groupRole = 'ADMIN'
        """)
    Optional<User> findAdminByProgramId(Long programId);

    long countByProgramIdAndProgramUserStatus(Long programId, ProgramUserStatus status); // -> 활성상태 인원 체크
}
