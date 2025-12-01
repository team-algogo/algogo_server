package com.ssafy.algogo.user.repository;

import com.ssafy.algogo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String userEmail);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    List<User> findByEmailContainingIgnoreCase(String content);

}
