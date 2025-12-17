package com.ssafy.algogo.program.problemset.service;

import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetCreateRequestDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetModifyRequestDto;
import com.ssafy.algogo.program.problemset.dto.response.CategoryListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.MyProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetProblemsPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import org.springframework.data.domain.Pageable;

public interface ProblemSetService {

	// 문제집 리스트 조회
	public ProblemSetListResponseDto getProblemSetList(String keyWord, String category,
		String sortBy,
		String sortDirection, int size,
		int page);

	// 자율 문제집 조회
	public ProblemSetResponseDto getProblemSet(Long programId);

	// 자율 문제집 생성
	public ProblemSetResponseDto createProblemSet(
		ProblemSetCreateRequestDto problemSetCreateRequestDto);

	// 자율 문제집 수정
	public ProblemSetResponseDto modifyProblemSet(Long programId, ProblemSetModifyRequestDto dto);

	// 자율 문제집 삭제
	public void deleteProblemSet(Long programId);

	// 문제집 문제 리스트 조회
	ProgramProblemPageResponseDto getProgramProblemsPage(
		Long programId,
		Pageable pageable
	);

	// 내가 참여한 문제집리스트 조회
	public MyProblemSetListResponseDto getMeJoinProblemSet(Long userId);

	// 문제집 카테고리 조회
	CategoryListResponseDto getCategoryList();
}
