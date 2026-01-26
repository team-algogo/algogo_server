package com.ssafy.algogo.program.problemset.service;

import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetCreateRequestDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetModifyRequestDto;
import com.ssafy.algogo.program.problemset.dto.response.CategoryListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.MyProblemSetPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetSearchPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetSearchResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetWithMatchPageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProblemSetService {

	// 문제집 리스트 조회
	ProblemSetListResponseDto getProblemSetList(String keyWord, String category,
			String sortBy,
			String sortDirection, int size,
			int page);

	// 자율 문제집 조회
	ProblemSetResponseDto getProblemSet(Long programId);

	// 자율 문제집 생성
	ProblemSetResponseDto createProblemSet(
			ProblemSetCreateRequestDto problemSetCreateRequestDto, MultipartFile thumbnail);

	// 자율 문제집 수정
	ProblemSetResponseDto modifyProblemSet(Long programId, ProblemSetModifyRequestDto dto, MultipartFile thumbnail);

	// 자율 문제집 삭제
	void deleteProblemSet(Long programId);

	// 문제집 문제 리스트 조회
	ProgramProblemPageResponseDto getProgramProblemsPage(
			Long programId,
			Pageable pageable);

	// 내가 참여한 문제집리스트 조회
	MyProblemSetPageResponseDto getMyJoinProblemSet(Long userId, Pageable pageable);

	// 문제집 카테고리 조회
	CategoryListResponseDto getCategoryList();

	// 문제집 제목/설명으로 검색 (페이지네이션)
	ProblemSetSearchPageResponseDto searchProblemSetByTitle(String keyword, Pageable pageable);

	// 문제집에 속한 문제로 검색 (페이지네이션)
	ProblemSetWithMatchPageResponseDto searchProblemSetByProblems(String keyword, Pageable pageable);
}
