package com.ssafy.algogo.program.group.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupRole {
    ADMIN(2),
    MANAGER(1),
    USER(0);

    private final int level;

    public boolean hasAtLeast(GroupRole required) {
        return this.level >= required.level;
    }
}
