package com.ssafy.algogo.submission.repository;

import com.ssafy.algogo.submission.entity.SubmissionAlgorithm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionAlgorithmRepository extends JpaRepository<SubmissionAlgorithm, Long> {

}
