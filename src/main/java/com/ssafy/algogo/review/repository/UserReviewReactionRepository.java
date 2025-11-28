package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.UserReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UserReviewReactionRepository extends JpaRepository<UserReviewReaction, Long> {

    void deleteByReview_Id(Long reviewId);

    UserReviewReaction findByUser_IdAndReview_Id(Long userId, Long reviewId);
}
