package com.ssafy.algogo.problem.dto.response;

import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public record ProgramProblemPageResponseDto (
        PageInfo page,
        SortInfo sort,
        List<ProgramProblemResponseDto> problemList
){
    public static ProgramProblemPageResponseDto from(Page<ProgramProblemResponseDto> programProblemResponseDto){
        return new ProgramProblemPageResponseDto(
                PageInfo.of(programProblemResponseDto),
                SortInfo.of(programProblemResponseDto),
                programProblemResponseDto.getContent()
        );
    }
}
