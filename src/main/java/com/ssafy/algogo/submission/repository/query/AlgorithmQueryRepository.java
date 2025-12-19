package com.ssafy.algogo.submission.repository.query;

import com.ssafy.algogo.submission.entity.Algorithm;
import java.util.List;

public interface AlgorithmQueryRepository {

  List<Algorithm> findByKeyword(String keyword);

  List<Algorithm> findAllAlgorithmsBySubmissionId(Long submissionId);
}
