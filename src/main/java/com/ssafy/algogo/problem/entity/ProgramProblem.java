package com.ssafy.algogo.problem.entity;

import com.ssafy.algogo.program.entity.Program;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ProgramProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "participant_count")
    private Long participantCount;

    @NotNull
    @Column(name = "submission_count")
    private Long submissionCount;

    @NotNull
    @Column(name = "solved_count")
    private Long solvedCount;

    @NotNull
    @Column(name = "view_count")
    private Long viewCount;

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
    private DifficultyViewType difficultyViewType = DifficultyViewType.PROBLEM_DIFFICULTY;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

}
