package com.ssafy.algogo.problem.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProgramProblemCreateRequestDto {
    @NotNull
    @Valid
    List<ProgramProblemRequestDto> programProblemRequestDtoList;
}
