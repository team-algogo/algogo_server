package com.ssafy.algogo.problem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type")
    private PlatformType platformType;

    @NotNull
    @Column(name = "problem_no")
    private String problemNo;

    @NotNull
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_type")
    private DifficultyType difficultyType;

    @NotNull
    @Column(name = "problem_link")
    private String problemLink;

}
