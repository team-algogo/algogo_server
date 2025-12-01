package com.ssafy.algogo.problem.entity;

import com.ssafy.algogo.common.utils.BaseTime;
import com.ssafy.algogo.problem.dto.request.ProgramProblemRequestDto;
import com.ssafy.algogo.program.entity.Program;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "programs_problems"
)
public class ProgramProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "participant_count")
    @Builder.Default
    private Long participantCount = 0L;

    @NotNull
    @Column(name = "submission_count")
    @Builder.Default
    private Long submissionCount = 0L;

    @NotNull
    @Column(name = "solved_count")
    @Builder.Default
    private Long solvedCount = 0L;

    @NotNull
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @NotNull
    @Column(name = "start_date")
    private LocalDateTime startDate; // 이거 Timestamp로 해야하나? LocalDateTime은 불변시간이라 음 음 되나?

    @NotNull
    @Column(name = "end_date")
    private LocalDateTime endDate; // 이거 Timestamp로 해야하나? LocalDateTime은 불변시간이라 음 음 되나?

    @Column(name = "user_difficulty_type")
    @Enumerated(EnumType.STRING)
    private UserDifficultyType userDifficultyType;

    @Column(name = "difficulty_view_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DifficultyViewType difficultyViewType = DifficultyViewType.PROBLEM_DIFFICULTY;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    public static ProgramProblem create(Program program, Problem problem, ProgramProblemRequestDto programProblemRequestDto){
        return ProgramProblem.builder()
                .program(program)
                .problem(problem)
                .startDate((programProblemRequestDto.getStartDate() != null) ? programProblemRequestDto.getStartDate() : LocalDateTime.now())
                .endDate((programProblemRequestDto.getEndDate() != null) ? programProblemRequestDto.getEndDate() : BaseTime.MYSQL_TIMESTAMP_MAX)
                .userDifficultyType((programProblemRequestDto.getUserDifficultyType() != null) ? programProblemRequestDto.getUserDifficultyType() : UserDifficultyType.MEDIUM)
                .difficultyViewType((programProblemRequestDto.getDifficultyViewType() != null) ? programProblemRequestDto.getDifficultyViewType() : DifficultyViewType.PROBLEM_DIFFICULTY)
                .build();
    }
}
