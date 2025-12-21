package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.problem.dto.response.ProgramProblemDetailResponseDto;
import com.ssafy.algogo.program.dto.response.ProgramResponseDto;
import com.ssafy.algogo.user.dto.response.UserSimpleResponseDto;

public record UserSubmissionResponseDto(
    UserSimpleResponseDto user,
    SubmissionResponseDto submission,
    ProgramProblemDetailResponseDto programProblem,
    ProgramResponseDto program
) {

}
