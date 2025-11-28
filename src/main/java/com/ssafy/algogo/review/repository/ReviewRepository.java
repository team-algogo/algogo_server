package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.Review;
import com.ssafy.algogo.review.repository.query.ReviewQueryRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryRepository {

    List<Review> findAllBySubmission_IdOrderByCreatedAtAsc(Long submissionId);

    List<Review> findAllByParentReview_Id(Long parentReviewId);

    void deleteAllByParentReview_Id(Long parentReviewId);

    boolean existsByUser_IdAndSubmission_IdAndParentReviewIsNull(Long userId, Long submissionId);

}