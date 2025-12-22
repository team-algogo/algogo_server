package com.ssafy.algogo.problem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemListResponseDto(List<ProblemResponseDto> problems) {

    public static ProblemListResponseDto from(List<ProblemResponseDto> problems) {
        return new ProblemListResponseDto(problems);
    }
}
