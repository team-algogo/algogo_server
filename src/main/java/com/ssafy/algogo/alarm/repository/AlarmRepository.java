package com.ssafy.algogo.alarm.repository;

import com.ssafy.algogo.alarm.entity.Alarm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("""
            delete from Alarm a
            where a.id in :ids
              and a.user.id = :userId
        """)
    void deleteAllByIdsAndUserId(List<Long> ids,
        Long userId); // 약간의 보안처리를 위해 삭제하려는 주체가 본인이 맞아야 되게 쿼리도 작성
}
