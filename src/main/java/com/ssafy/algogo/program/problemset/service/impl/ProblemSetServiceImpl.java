package com.ssafy.algogo.program.problemset.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.ProblemRepository;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramType;
import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetCreateRequestDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetModifyRequestDto;
import com.ssafy.algogo.program.problemset.dto.response.MyProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetProblemsPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import com.ssafy.algogo.program.problemset.service.ProblemSetService;
import com.ssafy.algogo.program.repository.ProgramRepository;
import com.ssafy.algogo.program.repository.ProgramTypeRepository;
import com.ssafy.algogo.program.repository.ProgramUserRepository;
import com.ssafy.algogo.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProblemSetServiceImpl implements ProblemSetService {

	private final ProblemRepository problemRepository;
	private final ProgramProblemRepository programProblemRepository;
	private final UserRepository userRepository;
	private final ProgramTypeRepository programTypeRepository;
	private final ProgramRepository programRepository;
	private final ProgramUserRepository programUserRepository;

	@Override
	public List<ProblemSetResponseDto> getProblemSetList(String keyword, String category,
		String sortBy, String sortDirection) {
		return null;
	}


	@Override
	public ProblemSetResponseDto getProblemSet(Long programId) {

		Program program = programRepository.findById(programId).orElseThrow(() ->
			new CustomException("해당 문제집을 찾을 수 없습니다.", ErrorCode.PROGRAM_ID_NOT_FOUND));

		return ProblemSetResponseDto.from(program);
	}

	@Override
	public ProblemSetResponseDto createProblemSet(
		ProblemSetCreateRequestDto problemSetCreateRequestDto) {

		if (programRepository.existsByTitle(problemSetCreateRequestDto.getTitle())) {
			throw new CustomException("이미 존재하는 문제집 제목입니다.",
				ErrorCode.PROBLEM_SET_ALREADY_EXISTS);
		}
		ProgramType programType = programTypeRepository.findByName("problemset").orElseThrow(
			() -> new CustomException("problemset 타입 데이터가 DB에 존재하지 않습니다.",
				ErrorCode.PROGRAM_TYPE_NOT_FOUND));

		Program program = Program.builder()
			.programType(programType)
			.title(problemSetCreateRequestDto.getTitle())
			.description(problemSetCreateRequestDto.getDescription())
			.thumbnail(problemSetCreateRequestDto.getThumbnail())
			.build();

		Program newProgram = programRepository.save(program);

		return ProblemSetResponseDto.from(newProgram);
	}

	@Override
	public ProblemSetResponseDto modifyProblemSet(Long programId,
		ProblemSetModifyRequestDto dto) {

		Program program = programRepository.findById(programId)
			.orElseThrow(
				() -> new CustomException("해당 문제집을 찾을 수 없습니다.", ErrorCode.PROGRAM_ID_NOT_FOUND));

		program.updateProgram(dto.getTitle(), dto.getDescription());
		// 프로텍티드로 되어서 변경이 안됨, 썸네일 왜 없음?

		program = programRepository.save(program);

		return ProblemSetResponseDto.from(program);
	}

	@Override
	public void deleteProblemSet(Long programId) {

		if (!programRepository.existsById(programId)) {
			throw new CustomException("삭제할 문제집이 존재하지 않습니다.", ErrorCode.PROGRAM_ID_NOT_FOUND);
		}

		programRepository.deleteById(programId);
	}


	@Override
	@Transactional(readOnly = true)
	public ProblemSetProblemsPageResponseDto getProgramProblemsPage(
		Long programId,
		boolean isLogined,
		String sortBy,
		String sortDirection,
		int size,
		int page
	) {
		if (!programRepository.existsById(programId)) {
			throw new CustomException("해당 문제집을 찾을 수 없습니다.", ErrorCode.PROGRAM_ID_NOT_FOUND);
		}

		Sort sort = Sort.by(
			"desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
			sortBy
		);
		Pageable pageable = PageRequest.of(page, size, sort);

		try {
			Page<ProgramProblem> programProblems =
				programProblemRepository.findAllByProgramId(programId, pageable);

			return ProblemSetProblemsPageResponseDto.of(
				isLogined, programProblems, sortBy, sortDirection
			);

		} catch (org.springframework.data.mapping.PropertyReferenceException e) {
			// 잘못된 필드명으로 정렬 시도 했을경우
			throw new CustomException("유효하지 않은 정렬 기준입니다: " + sortBy, ErrorCode.INVALID_PARAMETER);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public MyProblemSetListResponseDto getMeJoinProblemSet(Long userId) {

		List<ProgramUser> programUsers =
			programUserRepository.findAllByUserId(userId);

		List<ProblemSetResponseDto> programList = programUsers.stream()
			.map(ProgramUser::getProgram)
			.distinct()
			.map(ProblemSetResponseDto::from)
			.toList();

		return new MyProblemSetListResponseDto(programList);
	}
}