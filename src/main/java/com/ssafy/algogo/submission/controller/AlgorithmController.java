package com.ssafy.algogo.submission.controller;

import com.ssafy.algogo.common.advice.SuccessResponse;
import com.ssafy.algogo.submission.dto.response.AlgorithmListResponseDto;
import com.ssafy.algogo.submission.service.AlgorithmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/algorithms")
public class AlgorithmController {

  private final AlgorithmService algorithmService;

  @GetMapping("/search")
  public SuccessResponse searchAlgorithm(@RequestParam String keyword) {
    AlgorithmListResponseDto algorithmListResponseDto = algorithmService.searchAlgorithmWithKeyword(
        keyword);
    return new SuccessResponse("알고리즘 검색에 성공했습니다.", algorithmListResponseDto);
  }
}
