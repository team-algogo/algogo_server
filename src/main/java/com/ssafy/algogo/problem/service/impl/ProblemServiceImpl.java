package com.ssafy.algogo.problem.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.problem.dto.response.ProblemDetailResponseDto;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.ProblemRepository;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final ProgramProblemRepository programProblemRepository;

    @Override
    @Transactional(readOnly = true)
    public ProblemDetailResponseDto getProblem(Long programProblemId) {
        ProgramProblem programProblem = programProblemRepository.findById(programProblemId)
            .orElseThrow(() -> new CustomException("존재하지 않는 프로그램 문제입니다.",
                ErrorCode.PROGRAM_PROBLEM_NOT_FOUND));

        return ProblemDetailResponseDto.from(programProblem);
    }
}
