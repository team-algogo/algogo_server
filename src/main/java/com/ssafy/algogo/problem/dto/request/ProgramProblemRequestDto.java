package com.ssafy.algogo.problem.dto.request;

import com.ssafy.algogo.problem.entity.DifficultyViewType;
import com.ssafy.algogo.problem.entity.UserDifficultyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProgramProblemRequestDto {

    @NotNull(message = "problemId는 필수 값입니다.")
    private Long problemId;
    @FutureOrPresent(message = "startDate는 현재 시간 이후여야 합니다.")
    private LocalDateTime startDate;
    @FutureOrPresent(message = "endDate는 현재 시간 이후여야 합니다.")
    private LocalDateTime endDate;
    @Valid
    private UserDifficultyType userDifficultyType;
    @Valid
    private DifficultyViewType difficultyViewType;
}
