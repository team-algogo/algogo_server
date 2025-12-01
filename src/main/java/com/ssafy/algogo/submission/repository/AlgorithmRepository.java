package com.ssafy.algogo.submission.repository;

import com.ssafy.algogo.submission.entity.Algorithm;
import com.ssafy.algogo.submission.repository.query.AlgorithmQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlgorithmRepository extends JpaRepository<Algorithm, Long>,
    AlgorithmQueryRepository {

}
