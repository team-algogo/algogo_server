package com.ssafy.algogo.program.entity;

import com.ssafy.algogo.common.utils.BaseTime;
import com.ssafy.algogo.problem.entity.Problem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_program_title", columnNames = {"title"})
    }
)
public class Program extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String description;

    private String thumbnail;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_type_id")
    private ProgramType programType;

    protected Program(String title, String description, String thumbnail, ProgramType programType) {
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.programType = programType;
    }
}
