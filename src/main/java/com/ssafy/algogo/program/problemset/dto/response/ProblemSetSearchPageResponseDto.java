package com.ssafy.algogo.program.problemset.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemSetSearchPageResponseDto(
	PageInfoDto page,
	List<ProblemSetResponseDto> problemSetList
) {

	public record PageInfoDto(
		int number,
		int size,
		long totalElements,
		int totalPages
	) {

	}

	public static ProblemSetSearchPageResponseDto from(Page<ProblemSetResponseDto> page) {
		PageInfoDto pageInfo = new PageInfoDto(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
		return new ProblemSetSearchPageResponseDto(pageInfo, page.getContent());
	}
}