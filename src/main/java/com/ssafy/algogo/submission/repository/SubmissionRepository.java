package com.ssafy.algogo.submission.repository;

import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.submission.entity.Submission;
import com.ssafy.algogo.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

  List<Submission> findAllByUserAndProgramProblemOrderByCreatedAtAsc(User user,
      ProgramProblem programProblem);
}
