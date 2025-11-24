package com.ssafy.algogo.submission.entity;

import com.ssafy.algogo.common.utils.BaseTime;
import com.ssafy.algogo.problem.entity.ProgramProblem;
import com.ssafy.algogo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
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
    private String strategy;

    @NotNull
    @Column(name = "is_success")
    private Boolean isSuccess;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_problem_id")
    private ProgramProblem programProblem;



}
