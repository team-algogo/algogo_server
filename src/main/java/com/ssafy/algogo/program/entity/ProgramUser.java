package com.ssafy.algogo.program.entity;

import com.ssafy.algogo.common.utils.BaseTime;
import com.ssafy.algogo.program.group.entity.ProgramUserStatus;
import com.ssafy.algogo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Inheritance(strategy = InheritanceType.JOINED)
@Table(
    name = "programs_users"
)
public class ProgramUser extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "program_status")
    private ProgramUserStatus programUserStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    protected ProgramUser(ProgramUserStatus programUserStatus, Program program, User user) {
        this.programUserStatus = programUserStatus;
        this.program = program;
        this.user = user;
    }
}
