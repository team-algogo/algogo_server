package com.ssafy.algogo.problem.repository;

import com.ssafy.algogo.problem.entity.SubmissionAlgorithm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionAlgorithmRepository extends JpaRepository<SubmissionAlgorithm, Long> {

}
