package com.ssafy.algogo.problem.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.problem.dto.response.ProblemDetailResponseDto;
import com.ssafy.algogo.problem.dto.response.ProblemListResponseDto;
import com.ssafy.algogo.problem.dto.response.ProblemResponseDto;
import com.ssafy.algogo.problem.entity.Problem;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.ProblemRepository;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.problem.service.ProblemService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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

    @Override
    @Transactional(readOnly = true)
    public ProblemListResponseDto searchProblems(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ProblemListResponseDto.from(Collections.emptyList());
        }

        String escapeKeyword = escapeLike(keyword);

        List<Problem> problems = problemRepository.searchByKeyword(escapeKeyword);

        List<ProblemResponseDto> problemResponseDtos = new ArrayList<>();
        for (Problem problem : problems) {
            problemResponseDtos.add(ProblemResponseDto.from(problem));
        }

        return ProblemListResponseDto.from(problemResponseDtos);
    }

    private static String escapeLike(String s) {
        return s
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
}
