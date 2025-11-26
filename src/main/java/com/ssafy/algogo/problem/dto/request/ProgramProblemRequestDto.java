package com.ssafy.algogo.problem.dto.request;

import com.ssafy.algogo.problem.entity.DifficultyViewType;
import com.ssafy.algogo.problem.entity.UserDifficultyType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProgramProblemRequestDto {
    @NotNull
    private Long problemId;
    @FutureOrPresent
    private LocalDateTime startDate;
    @FutureOrPresent
    private LocalDateTime endDate;
    private UserDifficultyType userDifficultyType;
    private DifficultyViewType difficultyViewType;
}
