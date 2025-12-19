package com.ssafy.algogo.program.problemset.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProblemSetCreateRequestDto {

	private Long programId;
	private String title;
	private String description;
	private String thumbnail;
}
