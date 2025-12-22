package com.ssafy.algogo.problem.service;

import com.ssafy.algogo.problem.dto.response.ProblemDetailResponseDto;
import com.ssafy.algogo.problem.dto.response.ProblemListResponseDto;

public interface ProblemService {

    ProblemDetailResponseDto getProblem(Long programProblemId);

    ProblemListResponseDto searchProblems(String keyword);
}
