package com.ssafy.algogo.program.problemset.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.problem.repository.ProblemRepository;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.problem.service.ProgramProblemService;
import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramType;
import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetCreateRequestDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetModifyRequestDto;
import com.ssafy.algogo.program.problemset.dto.response.CategoryListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.CategoryResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.MyProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetProblemsPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetSearchResponseDto;
import com.ssafy.algogo.program.problemset.service.ProblemSetService;
import com.ssafy.algogo.program.repository.CategoryRepository;
import com.ssafy.algogo.program.repository.ProgramRepository;
import com.ssafy.algogo.program.repository.ProgramTypeRepository;
import com.ssafy.algogo.program.repository.ProgramUserRepository;
import com.ssafy.algogo.program.repository.query.ProgramQueryRepository;
import com.ssafy.algogo.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
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
	private final ProgramQueryRepository programQueryRepository;
	private final CategoryRepository categoryRepository;
	private final ProgramProblemService programProblemService;

	@Override
	@Transactional(readOnly = true)
	public ProblemSetListResponseDto getProblemSetList(
		String keyword,
		String category,
		String sortBy,
		String sortDirection,
		int size,
		int page
	) {
		// 1) size, page 보정
		if (size < 1) {
			size = 1;
		}
		if (size > 100) {
			size = 100;
		}
		if (page < 0) {
			page = 0;
		}

		// 2) problemset 타입 존재 여부 체크
		programTypeRepository.findByName("problemset")
			.orElseThrow(() -> new CustomException(
				"problemset 타입 없음", ErrorCode.PROGRAM_TYPE_NOT_FOUND
			));

		// 3) 리스트 조회
		List<ProblemSetResponseDto> list =
			programQueryRepository.findProblemSetWithCategoriesAndPopularity(
				keyword, category, sortBy, sortDirection, size, page
			);

		// 4) 전체 개수 조회
		long totalElements =
			programQueryRepository.countProblemSetWithFilter(keyword, category);

		// 5) PageInfo 직접 생성
		int totalPages = (int) Math.ceil((double) totalElements / size);
		PageInfo pageInfo = new PageInfo(
			page,          // number
			size,          // size
			totalElements, // totalElements
			totalPages     // totalPages
		);

		// 6) SortInfo 직접 생성
		SortInfo sortInfo = new SortInfo(
			sortBy,
			sortDirection.toUpperCase()
		);

		return new ProblemSetListResponseDto(pageInfo, sortInfo, list);
	}


	@Override
	@Transactional(readOnly = true)
	public ProblemSetResponseDto getProblemSet(Long programId) {

		ProblemSetResponseDto dto =
			programQueryRepository.findProblemSetDetail(programId);

		if (dto == null) {
			throw new CustomException(
				"해당 문제집을 찾을 수 없습니다.",
				ErrorCode.PROGRAM_ID_NOT_FOUND
			);
		}

		return dto;
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
	public ProgramProblemPageResponseDto getProgramProblemsPage(
		Long programId,
		Pageable pageable
	) {
		if (!programRepository.existsById(programId)) {
			throw new CustomException("해당 문제집을 찾을 수 없습니다.", ErrorCode.PROGRAM_ID_NOT_FOUND);
		}

		return programProblemService.getAllProgramProblems(programId, pageable);
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

	@Override
	public CategoryListResponseDto getCategoryList() {
		List<CategoryResponseDto> categoryResponseDtoList = categoryRepository.findAll()
			.stream()
			.map(CategoryResponseDto::from).toList();
		return new CategoryListResponseDto(categoryResponseDtoList);
	}

	@Override
	@Transactional(readOnly = true)
	public ProblemSetSearchResponseDto searchProblemSet(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return ProblemSetSearchResponseDto.from(Collections.emptyList());
		}

		String escapeKeyword = escapeLike(keyword);

		List<ProblemSetResponseDto> dtos =
			programQueryRepository.searchProblemSetByKeyword(escapeKeyword);

		return ProblemSetSearchResponseDto.from(dtos);
	}

	private static String escapeLike(String s) {
		return s
			.replace("\\", "\\\\")
			.replace("%", "\\%")
			.replace("_", "\\_");
	}
}