package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.UserReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UserReviewReactionRepository extends JpaRepository<UserReviewReaction, Long> {

}
