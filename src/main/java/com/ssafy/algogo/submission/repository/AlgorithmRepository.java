package com.ssafy.algogo.submission.repository;

import com.ssafy.algogo.submission.entity.Algorithm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlgorithmRepository extends JpaRepository<Algorithm, Long> {

}
