package com.ssafy.algogo.program.problemset.dto.response;


import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import java.util.List;

public record ProblemSetListResponseDto(
	PageInfo page,
	SortInfo sort,
	List<ProblemSetResponseDto> problemSetList
) {

}
