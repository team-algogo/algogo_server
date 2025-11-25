package com.ssafy.algogo.problem.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.problem.dto.request.ProgramProblemCreateRequestDto;
import com.ssafy.algogo.problem.dto.request.ProgramProblemRequestDto;
import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.problem.dto.response.ProgramProblemResponseDto;
import com.ssafy.algogo.problem.entity.Problem;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.ProblemRepository;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.problem.service.ProgramProblemService;
import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProgramProblemServiceImpl implements ProgramProblemService {
    private final ProgramRepository programRepository;
    private final ProblemRepository problemRepository;
    private final ProgramProblemRepository programProblemRepository;

    /*  Controller에서 사용 시, Pagable default 설정은 아래와 같이 작성
     *  @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    * */
    @Override
    @Transactional(readOnly = true)
    public ProgramProblemPageResponseDto getAllProgramProblems(Long programId, Pageable pageable) {
        Page<ProgramProblem> programProblems = programProblemRepository.findAllByProgramId(programId, pageable);
        return ProgramProblemPageResponseDto.from(
                programProblems.map(ProgramProblemResponseDto::from)
        );
    }

    @Override
    public void createProgramProblem(Long programId, ProgramProblemCreateRequestDto programProblemCreateRequestDto) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new CustomException("프로그램 정보가 잘못 되었습니다.", ErrorCode.NOT_FOUND));

        List<ProgramProblemRequestDto> programProblemRequestDtoList = programProblemCreateRequestDto.getProgramProblemRequestDtoList();
        List<Long> problemIdList = programProblemRequestDtoList.stream()
                .map(ProgramProblemRequestDto::getProblemId)
                .toList();
        List<Problem> problemList = problemRepository.findAllById(problemIdList);

        // 중복 제거 후 사이즈와 실제 DB 조회 사이즈가 다르면 없는 문제가 요청에 섞인 경우임.
        if(new HashSet<>(problemIdList).size() != problemList.size()){
            throw new CustomException("문제 정보가 잚못 되었습니다.", ErrorCode.BAD_REQUEST);
        }
        // 정상적인 요청인 경우 -> 프록시 객체로 넣어줌
        List<ProgramProblem> programProblemList = programProblemRequestDtoList.stream()
                .map(request -> ProgramProblem.create(program, problemRepository.getReferenceById(request.getProblemId()), request))
                .toList();

        programProblemRepository.saveAll(programProblemList);
    }

    @Override
    public void deleteProgramProblem(Long programId, List<Long> programProblemIds) {
        // 삭제 수행 여부에 관계 없이, 잘못된 프로그램 정보로, 요청 자체가 문제인 경우
        programRepository.findById(programId)
                .orElseThrow(() -> new CustomException("프로그램 정보가 잘못 되었습니다.", ErrorCode.NOT_FOUND));
        programProblemRepository.deleteAllById(programProblemIds);
    }
}
