package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.RequireReview;
import com.ssafy.algogo.review.entity.Review;
import com.ssafy.algogo.review.repository.query.RequireReviewQueryRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequireReviewRepository extends JpaRepository<RequireReview, Long>,
    RequireReviewQueryRepository {

    Optional<RequireReview> findByUser_IdAndSubmission_Id(Long userId,
        Long submissionId);
}
