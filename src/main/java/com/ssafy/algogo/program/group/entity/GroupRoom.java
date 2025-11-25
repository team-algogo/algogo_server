package com.ssafy.algogo.program.group.entity;

import com.ssafy.algogo.program.entity.Program;
import com.ssafy.algogo.program.entity.ProgramType;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GroupRoom extends Program {

    @NotNull
    private Long capacity;

    private GroupRoom(String title, String description, ProgramType programType, Long capacity) {
        super(title, description, null, programType);
        this.capacity = capacity;
    }

    public static GroupRoom create(String title, String description, ProgramType programType, Long capacity) {
        return new GroupRoom(title, description, programType, capacity);
    }}
