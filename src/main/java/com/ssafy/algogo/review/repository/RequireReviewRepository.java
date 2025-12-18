package com.ssafy.algogo.review.repository;

import com.ssafy.algogo.review.entity.RequireReview;
import com.ssafy.algogo.review.repository.query.RequireReviewQueryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RequireReviewRepository extends JpaRepository<RequireReview, Long>,
    RequireReviewQueryRepository {

    Optional<RequireReview> findBySubjectUserIdAndTargetSubmissionId(Long userId,
        Long submissionId);

    List<RequireReview> findAllByTargetSubmissionIdAndIsDone(Long targetSubmissionId,
        Boolean isDone);

    @Modifying
    @Query("""
            delete from RequiredReview rr
            where rr.subjectUser.id = :userId
              and rr.subjectSubmission.id in (
                  select s.id
                  from Submission s
                  join s.programProblem pp
                  where pp.program.id = :programId
              )
        """)
    void deleteRequiredReviewsByUserAndProgram(Long userId, Long programId);
}
