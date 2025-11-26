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
@Table(
    name = "group_rooms_users"
)
public class GroupsUser extends ProgramUser {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "group_role")
    private GroupRole groupRole;

    // 명시적 생성자 추가
    private GroupsUser(ProgramUserStatus programUserStatus, Program program, User user, GroupRole groupRole) {
        super(programUserStatus, program, user);
        this.groupRole = groupRole;
    }

    // 정적 팩토리 메서드 추가
    public static GroupsUser create(ProgramUserStatus programUserStatus, Program program, User user, GroupRole groupRole) {
        return new GroupsUser(programUserStatus, program, user, groupRole);
    }
}
