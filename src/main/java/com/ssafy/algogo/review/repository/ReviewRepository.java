package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  List<Review> findAllBySubmission_IdOrderByCreatedAtAsc(Long submissionId);
}
