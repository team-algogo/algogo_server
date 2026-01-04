package com.ssafy.algogo.program.problemset.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemSetSearchResponseDto(
	List<ProblemSetResponseDto> problemSetList
) {

	public static ProblemSetSearchResponseDto from(List<ProblemSetResponseDto> list) {
		return new ProblemSetSearchResponseDto(list);
	}
}