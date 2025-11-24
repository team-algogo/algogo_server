package com.ssafy.algogo.problem.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.problem.dto.ProblemResponseDto;
import com.ssafy.algogo.problem.entity.Problem;
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
    public Problem getProblem(Long programProblemId) {
        return programProblemRepository.findById(programProblemId)
                .orElseThrow(() -> new CustomException("프로그램 문제 정보가 잘못 되었습니다.", ErrorCode.BAD_REQUEST_ERROR))
                .getProblem();
    }
}
