package com.ssafy.algogo.submission.repository;

import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.submission.repository.query.SubmissionQueryRepository;
import com.ssafy.algogo.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Long>,
    SubmissionQueryRepository {

    List<Submission> findAllByUserAndProgramProblemOrderByCreatedAtAsc(User user,
        ProgramProblem programProblem);

    @Query("""
            select s
            from Submission s
            join fetch s.user u
            join fetch s.programProblem pp
            join fetch pp.program p
            join fetch pp.problem pr
            where s.id = :submissionId
        """)
    Optional<Submission> findByIdWithAll(@Param("submissionId") Long submissionId);

    boolean existsByUserIdAndProgramProblemId(Long userId, Long programProblemId);

    Long countSubmissionsByProgramProblemId(Long programProblemId);

    Long countSubmissionsByProgramProblemIdAndIsSuccess(Long programProblemId, Boolean isSuccess);
}
