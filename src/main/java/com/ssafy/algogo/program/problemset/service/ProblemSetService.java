package com.ssafy.algogo.program.problemset.service;

import com.ssafy.algogo.program.problemset.dto.request.ProblemSetCreateRequestDto;
import com.ssafy.algogo.program.problemset.dto.request.ProblemSetModifyRequestDto;
import com.ssafy.algogo.program.problemset.dto.response.MyProblemSetListResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetProblemsPageResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import java.util.List;

public interface ProblemSetService {

	// 문제집 리스트 조회
	public List<ProblemSetResponseDto> getProblemSetList(String keyWord, String category,
		String sortBy,
		String sortDirection);

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
	public ProblemSetProblemsPageResponseDto getProgramProblemsPage(
		Long programId,
		boolean isLogined,
		String sortBy,
		String sortDirection,
		int size,
		int page
	);

	// 내가 참여한 문제집리스트 조회
	public MyProblemSetListResponseDto getMeJoinProblemSet(Long userId);

}
