package com.ssafy.algogo.submission.service.impl;

import com.ssafy.algogo.submission.dto.response.AlgorithmListResponseDto;
import com.ssafy.algogo.submission.repository.AlgorithmKeywordRepository;
import com.ssafy.algogo.submission.repository.AlgorithmRepository;
import com.ssafy.algogo.submission.service.AlgorithmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmService {

  private final AlgorithmRepository algorithmRepository;
  private final AlgorithmKeywordRepository algorithmKeywordRepository;

  @Override
  public AlgorithmListResponseDto searchAlgorithmWithKeyword(String keyword) {
    return null;
  }
}
