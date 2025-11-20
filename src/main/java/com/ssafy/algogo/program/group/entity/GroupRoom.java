package com.ssafy.algogo.program.group.entity;

import com.ssafy.algogo.program.entity.Program;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class GroupRoom extends Program {

    @NotNull
    private Long capacity;

}
