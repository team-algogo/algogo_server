package com.ssafy.algogo.program.group.entity;

import com.ssafy.algogo.program.entity.Program;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class GroupRoom extends Program {

    @NotNull
    private Long capacity;

}
