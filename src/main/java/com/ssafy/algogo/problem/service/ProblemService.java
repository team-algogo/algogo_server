package com.ssafy.algogo.problem.service;

import com.ssafy.algogo.problem.dto.response.ProblemDetailResponseDto;

public interface ProblemService {

    ProblemDetailResponseDto getProblem(Long programProblemId);
}
