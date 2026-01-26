package com.ssafy.algogo.program.problemset.service.impl;

import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import com.ssafy.algogo.common.utils.S3Service;
import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.problem.repository.ProblemRepository;
import com.ssafy.algogo.problem.repository.ProgramProblemRepository;
import com.ssafy.algogo.problem.service.ProgramProblemService;
import com.ssafy.algogo.program.entity.Category;
import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramCategory;
import com.ssafy.algogo.program.entity.ProgramType;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetCreateRequestDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetModifyRequestDto;
import com.ssafy.algogo.program.problemset.dto.response.CategoryListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.CategoryResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.MyProblemSetPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetSearchPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetWithMatchPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetWithMatchResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetWithProgressResponseDto;
import com.ssafy.algogo.program.problemset.service.ProblemSetService;
import com.ssafy.algogo.program.repository.CategoryRepository;
import com.ssafy.algogo.program.repository.ProgramCategoryRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.algogo.submission.repository.SubmissionRepository;

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
	private final S3Service s3Service;
	private final ProgramCategoryRepository programCategoryRepository;
	private final SubmissionRepository submissionRepository;

	@Override
	@Transactional(readOnly = true)
	public ProblemSetListResponseDto getProblemSetList(
			String keyword,
			String category,
			String sortBy,
			String sortDirection,
			int size,
			int page) {
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
						"problemset 타입 없음", ErrorCode.PROGRAM_TYPE_NOT_FOUND));

		// 3) 리스트 조회
		List<ProblemSetResponseDto> list = programQueryRepository.findProblemSetWithCategoriesAndPopularity(
				keyword, category, sortBy, sortDirection, size, page);

		// 4) 전체 개수 조회
		long totalElements = programQueryRepository.countProblemSetWithFilter(keyword, category);

		// 5) PageInfo 직접 생성
		int totalPages = (int) Math.ceil((double) totalElements / size);
		PageInfo pageInfo = new PageInfo(
				page, // number
				size, // size
				totalElements, // totalElements
				totalPages // totalPages
		);

		// 6) SortInfo 직접 생성
		SortInfo sortInfo = new SortInfo(
				sortBy,
				sortDirection.toUpperCase());

		return new ProblemSetListResponseDto(pageInfo, sortInfo, list);
	}

	@Override
	@Transactional(readOnly = true)
	public ProblemSetResponseDto getProblemSet(Long programId) {

		ProblemSetResponseDto dto = programQueryRepository.findProblemSetDetail(programId);

		if (dto == null) {
			throw new CustomException(
					"해당 문제집을 찾을 수 없습니다.",
					ErrorCode.PROGRAM_ID_NOT_FOUND);
		}

		return dto;
	}

	@Override
	@Transactional
	public ProblemSetResponseDto createProblemSet(
			ProblemSetCreateRequestDto createRequestDto,
			MultipartFile thumbnail) {

		// 중복 제목 검사
		if (programRepository.existsByTitle(createRequestDto.getTitle())) {
			throw new CustomException(
					"이미 존재하는 문제집 제목입니다.",
					ErrorCode.PROBLEM_SET_ALREADY_EXISTS);
		}

		// ProgramType 조회
		ProgramType programType = programTypeRepository.findByName("problemset")
				.orElseThrow(() -> new CustomException(
						"problemset 타입 데이터가 DB에 존재하지 않습니다.",
						ErrorCode.PROGRAM_TYPE_NOT_FOUND));

		// 파일 검증 & S3 업로드
		if (thumbnail == null || thumbnail.isEmpty()) {
			throw new CustomException("썸네일 파일이 필수입니다.", ErrorCode.BAD_REQUEST);
		}
		String thumbnailUrl = s3Service.uploadProblemsetThumbnail(thumbnail);
		log.info("S3 업로드 완료: {}", thumbnailUrl);

		// Program 엔티티 생성 & 저장
		Program program = Program.builder()
				.programType(programType)
				.title(createRequestDto.getTitle())
				.description(createRequestDto.getDescription())
				.thumbnail(thumbnailUrl)
				.build();
		Program newProgram = programRepository.save(program);

		// 카테고리 연관관계 저장 (DB에만)
		List<String> savedCategories = new ArrayList<>();
		if (createRequestDto.getCategories() != null && !createRequestDto.getCategories()
				.isEmpty()) {
			createRequestDto.getCategories().forEach(categoryName -> {
				Category category = categoryRepository.findByName(categoryName)
						.orElseThrow(() -> new CustomException(
								categoryName + " 카테고리가 존재하지 않습니다.",
								ErrorCode.BAD_REQUEST));

				ProgramCategory programCategory = ProgramCategory.builder()
						.program(newProgram)
						.category(category)
						.build();
				programCategoryRepository.save(programCategory);
				savedCategories.add(categoryName);
			});
			log.info("카테고리 저장 완료: {}", savedCategories);
		} else {
			log.warn("카테고리 없음 or 빈 리스트");
		}

		// Response 직접 생성 (Program 없이, categories 포함!)
		return new ProblemSetResponseDto(
				newProgram.getId(),
				newProgram.getTitle(),
				newProgram.getDescription(),
				newProgram.getThumbnail(),
				newProgram.getCreatedAt(),
				newProgram.getModifiedAt(),
				newProgram.getProgramType().getName(),
				savedCategories,
				0L,
				0L);
	}

	@Override
	@Transactional
	public ProblemSetResponseDto modifyProblemSet(Long programId,
			ProblemSetModifyRequestDto modifyRequestDto, MultipartFile thumbnail) {
		log.info("문제집 수정 시작: ID={}, categories={}", programId, modifyRequestDto.getCategories());

		// 1. Program 조회
		Program program = programRepository.findById(programId)
				.orElseThrow(() -> new CustomException("문제집 없음", ErrorCode.PROGRAM_ID_NOT_FOUND));

		// 2. 기본 정보 업데이트
		if (thumbnail != null && !thumbnail.isEmpty()) {
			// 기존 S3 삭제
			if (program.getThumbnail() != null) {
				s3Service.deleteImage(program.getThumbnail());
			}
			// 새 썸네일 업로드
			String newThumbnailUrl = s3Service.uploadProblemsetThumbnail(
					thumbnail);
			program.updateProgram(modifyRequestDto.getTitle(), modifyRequestDto.getDescription(),
					newThumbnailUrl);
			log.info("썸네일 변경: {}", newThumbnailUrl);
		} else {
			program.updateProgram(modifyRequestDto.getTitle(), modifyRequestDto.getDescription());
		}

		// 3. Program 저장
		Program updatedProgram = programRepository.save(program);
		log.info("기본 정보 업데이트 완료");

		// 4. 카테고리 전체 교체
		programCategoryRepository.deleteByProgramId(programId);
		log.info("기존 카테고리 삭제 완료");

		List<String> savedCategories = new ArrayList<>();
		if (modifyRequestDto.getCategories() != null && !modifyRequestDto.getCategories()
				.isEmpty()) {
			log.info("새 카테고리 저장 시작: {}", modifyRequestDto.getCategories());
			for (String categoryName : modifyRequestDto.getCategories()) {
				Category category = categoryRepository.findByName(categoryName)
						.orElseThrow(() -> new CustomException(
								categoryName + " 카테고리가 존재하지 않습니다.",
								ErrorCode.BAD_REQUEST));

				ProgramCategory programCategory = ProgramCategory.builder()
						.program(updatedProgram)
						.category(category)
						.build();
				programCategoryRepository.save(programCategory);
				savedCategories.add(categoryName);
			}
			log.info("카테고리 저장 완료: {}", savedCategories);
		} else {
			log.info("카테고리 변경 없음");
		}

		// 5. Response 생성
		return new ProblemSetResponseDto(
				updatedProgram.getId(),
				updatedProgram.getTitle(),
				updatedProgram.getDescription(),
				updatedProgram.getThumbnail(),
				updatedProgram.getCreatedAt(),
				updatedProgram.getModifiedAt(),
				updatedProgram.getProgramType().getName(),
				savedCategories.isEmpty() ? List.of() : savedCategories, // 빈 경우 빈 리스트
				0L,
				0L);
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
			Pageable pageable) {
		if (!programRepository.existsById(programId)) {
			throw new CustomException("해당 문제집을 찾을 수 없습니다.", ErrorCode.PROGRAM_ID_NOT_FOUND);
		}

		return programProblemService.getAllProgramProblems(programId, pageable);
	}

	// 내가 참여한 문제집 조회 (페이징 + 정렬)
	@Override
	@Transactional(readOnly = true)
	public MyProblemSetPageResponseDto getMyJoinProblemSet(Long userId, Pageable pageable) {

		// 사용자가 참여한 문제집 ID 조회 (제출 이력 기반)
		List<Long> programIds = submissionRepository.findProgramIdsByUserId(userId);

		// 참여한 문제집이 없으면 빈 페이지 반환
		if (programIds.isEmpty()) {
			return MyProblemSetPageResponseDto.from(Page.empty(pageable));
		}

		// ID 리스트로 페이징된 문제집 조회 (QueryDSL)
		Page<ProblemSetWithProgressResponseDto> page = programQueryRepository.findMyJoinProblemSets(
				userId, programIds, pageable);

		// DTO로 변환
		return MyProblemSetPageResponseDto.from(page);
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
	public ProblemSetSearchPageResponseDto searchProblemSetByTitle(
			String keyword, Pageable pageable) {

		if (keyword == null || keyword.isBlank()) {
			return ProblemSetSearchPageResponseDto.from(
					new PageImpl<>(Collections.emptyList(), pageable, 0));
		}

		String escapeKeyword = escapeLike(keyword);
		Page<ProblemSetResponseDto> result = programQueryRepository.searchProblemSetByTitleOrDescription(escapeKeyword,
				pageable);

		return ProblemSetSearchPageResponseDto.from(result);
	}

	@Override
	@Transactional(readOnly = true)
	public ProblemSetWithMatchPageResponseDto searchProblemSetByProblems(
			String keyword, Pageable pageable) {

		if (keyword == null || keyword.isBlank()) {
			return ProblemSetWithMatchPageResponseDto.from(
					new PageImpl<>(Collections.emptyList(), pageable, 0));
		}

		String escapeKeyword = escapeLike(keyword);
		Page<ProblemSetWithMatchResponseDto> result = programQueryRepository.searchProblemSetByProblems(escapeKeyword,
				pageable);

		return ProblemSetWithMatchPageResponseDto.from(result);
	}

	private static String escapeLike(String s) {
		return s
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
	}

}