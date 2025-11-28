package com.ssafy.algogo.submission.dto.response;

import java.util.List;

public record AlgorithmListResponseDto(
    List<AlgorithmResponseDto> algorithmList
) {

  public AlgorithmListResponseDto from(List<AlgorithmResponseDto> algorithmList) {
    return new AlgorithmListResponseDto(algorithmList);
  }
}
