package com.ssafy.algogo.program.problemset.dto.response;


import com.ssafy.algogo.program.entity.Program;
import java.util.List;

public record ProblemSetListResponseDto(
	List<ProblemSetResponseDto> problemSetList
) {

}
