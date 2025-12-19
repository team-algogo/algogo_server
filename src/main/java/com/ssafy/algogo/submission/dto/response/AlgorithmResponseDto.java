package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.submission.entity.Algorithm;

public record AlgorithmResponseDto(
    Long id,
    String name
) {

  public static AlgorithmResponseDto from(Algorithm algorithm) {
    return new AlgorithmResponseDto(
        algorithm.getId(),
        algorithm.getName()
    );
  }
}
