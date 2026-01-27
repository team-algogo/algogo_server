package com.ssafy.algogo.problem.entity;

import com.ssafy.algogo.common.utils.BaseTime;
import com.ssafy.algogo.problem.dto.request.ProgramProblemRequestDto;
import com.ssafy.algogo.program.entity.Program;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "program_id")
    private Program program;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    public static ProgramProblem create(Program program, Problem problem,
        ProgramProblemRequestDto programProblemRequestDto) {
        return ProgramProblem.builder()
            .program(program)
            .problem(problem)
            .startDate((programProblemRequestDto.getStartDate() != null)
                ? programProblemRequestDto.getStartDate() : LocalDateTime.now())
            .endDate((programProblemRequestDto.getEndDate() != null)
                ? programProblemRequestDto.getEndDate() : BaseTime.MYSQL_TIMESTAMP_MAX)
            .userDifficultyType((programProblemRequestDto.getUserDifficultyType() != null)
                ? programProblemRequestDto.getUserDifficultyType() : UserDifficultyType.MEDIUM)
            .difficultyViewType((programProblemRequestDto.getDifficultyViewType() != null)
                ? programProblemRequestDto.getDifficultyViewType()
                : DifficultyViewType.PROBLEM_DIFFICULTY)
            .build();
    }

    public void increaseSubmissionCount() {
        this.submissionCount++;
    }

    public void increaseSolvedCount() {
        this.solvedCount++;
    }

    public void increaseParticipantCount() {
        this.participantCount++;
    }

    public void increaseViewCount(){
        this.viewCount++;
    }

    public void reflectDeletion(boolean isSuccess){
        this.participantCount--;
        this.submissionCount--;
        if (isSuccess) {
            this.solvedCount--;
        }
    }
}
