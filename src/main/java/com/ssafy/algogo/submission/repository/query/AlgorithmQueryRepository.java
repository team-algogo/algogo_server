package com.ssafy.algogo.submission.repository.query;

import com.ssafy.algogo.submission.entity.Algorithm;

public interface AlgorithmQueryRepository {

  Algorithm findByKeyword(String keyword);
}
