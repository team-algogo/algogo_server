package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.RequireReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequireReviewRepository extends JpaRepository<RequireReview, Long> {

}
