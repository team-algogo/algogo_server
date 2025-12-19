package com.ssafy.algogo.program.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

import com.ssafy.algogo.program.group.entity.GroupRole;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupRoomResponseDto(
    Long programId,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    Long capacity,
    Long memberCount,
    Long programProblemCount,
    Boolean isMember,
    GroupRole groupRole
) {

}
