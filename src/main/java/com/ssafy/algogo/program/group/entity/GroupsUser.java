package com.ssafy.algogo.program.group.entity;

import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramUser;
import com.ssafy.algogo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GroupsUser extends ProgramUser {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "group_role")
    private GroupRole groupRole;

    // 명시적 생성자 추가
    private GroupsUser(ProgramStatus programStatus, Program program, User user, GroupRole groupRole) {
        super(programStatus, program, user);
        this.groupRole = groupRole;
    }

    // 정적 팩토리 메서드 추가
    public static GroupsUser create(ProgramStatus programStatus, Program program, User user, GroupRole groupRole) {
        return new GroupsUser(programStatus, program, user, groupRole);
    }
}
