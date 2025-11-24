package com.ssafy.algogo.program.group.repository;

import com.ssafy.algogo.program.group.entity.GroupsUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupsUser, Long> {

}
