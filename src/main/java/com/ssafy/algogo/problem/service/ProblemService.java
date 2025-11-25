package com.ssafy.algogo.problem.service;

import com.ssafy.algogo.problem.dto.response.ProblemResponseDto;

public interface ProblemService {
    ProblemResponseDto getProblem(Long programProblemId);
}
