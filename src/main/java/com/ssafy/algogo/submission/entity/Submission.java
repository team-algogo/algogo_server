package com.ssafy.algogo.submission.entity;

import com.ssafy.algogo.common.utils.BaseTime;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    name = "submissions"
)
public class Submission extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String language;

    @NotNull
    private String code;

    @NotNull
    @Column(name = "exec_time")
    private Long execTime;

    @NotNull
    private Long memory;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String strategy;

    @NotNull
    @Column(name = "is_success")
    private Boolean isSuccess;

    @NotNull
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "program_problem_id")
    private ProgramProblem programProblem;

    public void increaseViewCount() {
        this.viewCount++;
    }
}
