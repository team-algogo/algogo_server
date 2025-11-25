package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.RequireReview;
import com.ssafy.algogo.review.repository.query.RequireReviewQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequireReviewRepository extends JpaRepository<RequireReview, Long>,
    RequireReviewQueryRepository {

}
