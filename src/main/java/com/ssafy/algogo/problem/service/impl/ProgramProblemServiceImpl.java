package com.ssafy.algogo.problem.service.impl;

import com.ssafy.algogo.problem.repository.ProblemRepository;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.problem.service.ProgramProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProgramProblemServiceImpl implements ProgramProblemService {
    private ProblemRepository problemRepository;
    private ProgramProblemRepository programProblemRepository;

    @Override
    public void createProgramProblem(Long programId, Long problemId) {

    }
}
