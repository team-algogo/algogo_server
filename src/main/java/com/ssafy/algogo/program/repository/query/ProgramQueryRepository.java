package com.ssafy.algogo.program.repository.query;

import com.ssafy.algogo.program.problemset.dto.response.ProblemSetResponseDto;
import com.ssafy.algogo.program.problemset.dto.response.ProblemSetWithMatchResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProgramQueryRepository {

	/**
	 * 자율 문제집(problemset) 리스트 조회 - keyword: Program.title 에 대한 검색 - categoryName:
	 * Category.name 필터 -
	 * sortBy: createdAt | popular(참여자수 합) | title - sortDirection: asc | desc
	 */
	List<ProblemSetResponseDto> findProblemSetWithCategoriesAndPopularity(
			String keyword,
			String categoryName,
			String sortBy,
			String sortDirection,
			int size,
			int page);

	long countProblemSetWithFilter(
			String keyword,
			String categoryName);

	ProblemSetResponseDto findProblemSetDetail(Long programId);

	Page<ProblemSetResponseDto> searchProblemSetByTitleOrDescription(
			String keyword, Pageable pageable);

	Page<ProblemSetWithMatchResponseDto> searchProblemSetByProblems(
			String keyword, Pageable pageable);

	Page<ProblemSetResponseDto> findMyJoinProblemSets(
			List<Long> programIds,
			Long userId,
			Pageable pageable);
}