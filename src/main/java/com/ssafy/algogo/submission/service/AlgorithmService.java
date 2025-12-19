package com.ssafy.algogo.submission.service;

import com.ssafy.algogo.submission.dto.response.AlgorithmListResponseDto;

public interface AlgorithmService {

    AlgorithmListResponseDto searchAlgorithmWithKeyword(String keyword);
}
