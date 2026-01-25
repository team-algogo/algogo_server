package com.ssafy.algogo.program.problemset.dto.response;

import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import java.util.List;
import org.springframework.data.domain.Page;

public record MyProblemSetPageResponseDto(
	PageInfo page,
	SortInfo sort,
	List<ProblemSetWithProgressResponseDto> problemSetLists
) {

	public static MyProblemSetPageResponseDto from(
		Page<ProblemSetWithProgressResponseDto> pageData) {
		return new MyProblemSetPageResponseDto(
			PageInfo.of(pageData),
			SortInfo.of(pageData),
			pageData.getContent()
		);
	}
}
