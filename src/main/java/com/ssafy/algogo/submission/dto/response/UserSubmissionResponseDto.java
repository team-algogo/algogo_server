package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.problem.dto.response.ProgramProblemResponseDto;
import com.ssafy.algogo.program.dto.response.ProgramResponseDto;

public record UserSubmissionResponseDto(
    SubmissionResponseDto submission,
    ProgramProblemResponseDto programProblem,
    ProgramResponseDto program
) {

}
