package com.ssafy.algogo.submission.repository;

import com.ssafy.algogo.submission.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

}
