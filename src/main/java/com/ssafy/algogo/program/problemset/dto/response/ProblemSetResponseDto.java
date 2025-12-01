package com.ssafy.algogo.program.problemset.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.program.entity.Program;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemSetResponseDto(

	Long programId,
	String title,
	String description,
	String thumbnail,
	LocalDateTime createAt,
	LocalDateTime modifiedAt,
	String programType
) {

	public static ProblemSetResponseDto from(Program program) {
		return new ProblemSetResponseDto(
			program.getId(),
			program.getTitle(),
			program.getDescription(),
			program.getThumbnail(),
			program.getCreatedAt(),
			program.getModifiedAt(),
			program.getProgramType().getName()
		);
	}
}
