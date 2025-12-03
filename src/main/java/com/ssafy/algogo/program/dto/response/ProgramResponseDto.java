package com.ssafy.algogo.program.dto.response;

public record ProgramResponseDto(
    Long id,
    String title,
    String thumbnail,
    ProgramTypeResponseDto programType
) {

}
