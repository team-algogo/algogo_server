package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.problem.dto.response.ProblemResponseDto;
import com.ssafy.algogo.program.dto.response.ProgramResponseDto;

public record SubmissionMeResponseDto(
    SubmissionResponseDto submissionResponseDto,
    Long reviewCount,
    ProblemResponseDto problemResponseDto,
    ProgramResponseDto programResponseDto
) {

}
