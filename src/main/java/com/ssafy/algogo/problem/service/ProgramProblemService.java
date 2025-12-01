package com.ssafy.algogo.problem.service;

import com.ssafy.algogo.problem.dto.request.ProgramProblemCreateRequestDto;
import com.ssafy.algogo.problem.dto.request.ProgramProblemDeleteRequestDto;
import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.problem.dto.request.ProgramProblemRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProgramProblemService {

    ProgramProblemPageResponseDto getAllProgramProblems(Long programId, Pageable pageable);

    void createProgramProblem(Long programId,
        ProgramProblemCreateRequestDto programProblemCreateRequestDto);

    void deleteProgramProblem(Long programId,
        ProgramProblemDeleteRequestDto programProblemDeleteRequestDto);
}
