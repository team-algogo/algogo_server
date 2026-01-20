package com.ssafy.algogo.program.problemset.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemSetWithMatchPageResponseDto(
	PageInfoDto page,
	List<ProblemSetWithMatchResponseDto> problemSetList) {

	public record PageInfoDto(
		int number,
		int size,
		long totalElements,
		int totalPages) {

	}

	public static ProblemSetWithMatchPageResponseDto from(
		Page<ProblemSetWithMatchResponseDto> page) {
		PageInfoDto pageInfo = new PageInfoDto(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages());
		return new ProblemSetWithMatchPageResponseDto(pageInfo, page.getContent());
	}
}
