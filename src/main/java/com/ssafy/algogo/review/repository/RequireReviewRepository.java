package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.RequireReview;
import com.ssafy.algogo.review.repository.query.RequireReviewQueryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequireReviewRepository extends JpaRepository<RequireReview, Long>,
    RequireReviewQueryRepository {

    Optional<RequireReview> findBySubjectUserIdAndTargetSubmissionId(Long userId,
        Long submissionId);

    List<RequireReview> findAllByTargetSubmissionIdAndIsDone(Long targetSubmissionId,
        Boolean isDone);

}
