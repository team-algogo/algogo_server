package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}
