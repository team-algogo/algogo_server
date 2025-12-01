package com.ssafy.algogo.program.group.repository;

import com.ssafy.algogo.program.group.entity.GroupRoom;
import com.ssafy.algogo.program.group.repository.query.GroupQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface GroupRepository extends JpaRepository<GroupRoom, Long>, GroupQueryRepository {

}
