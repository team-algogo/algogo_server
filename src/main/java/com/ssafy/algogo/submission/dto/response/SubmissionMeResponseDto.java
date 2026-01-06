package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.problem.dto.response.ProblemResponseDto;
import com.ssafy.algogo.program.dto.response.ProgramResponseDto;
import com.ssafy.algogo.user.dto.response.UserSimpleResponseDto;

public record SubmissionMeResponseDto(
    UserSimpleResponseDto userSimpleResponseDto,
    SubmissionResponseDto submissionResponseDto,
    Long reviewCount,
    ProblemResponseDto problemResponseDto,
    ProgramResponseDto programResponseDto
) {

}
