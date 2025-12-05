package com.ssafy.algogo.alarm.repository;

import com.ssafy.algogo.alarm.entity.AlarmType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmTypeRepository extends JpaRepository<AlarmType, Long> {

    Optional<AlarmType> findByName(String name);
}