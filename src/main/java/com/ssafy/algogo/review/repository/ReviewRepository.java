package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.Review;
import com.ssafy.algogo.review.repository.query.ReviewQueryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryRepository {

    List<Review> findAllBySubmission_IdOrderByCreatedAtAsc(Long submissionId);

    boolean existsByUser_IdAndSubmission_IdAndParentReviewIsNull(Long userId, Long submissionId);

    @Query("""
            select r
            from Review r
            join fetch r.user
            where r.id = :reviewId
        """)
    Optional<Review> findByIdWithUser(@Param("reviewId") Long reviewId);

}