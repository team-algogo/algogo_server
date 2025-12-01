package com.ssafy.algogo.program.problemset.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProgramProblemsDeleteRequestDto {

	private List<Long> programProblemIds;  // 클라이언트에서 원하는 필드명
}
