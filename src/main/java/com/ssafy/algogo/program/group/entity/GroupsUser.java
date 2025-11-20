package com.ssafy.algogo.program.group.entity;

import com.ssafy.algogo.program.entity.ProgramUser;
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

}
