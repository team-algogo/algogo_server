package com.ssafy.algogo.program.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.program.group.entity.GroupRole;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyGroupRoomResponseDto(
    Long programId,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    Long capacity,
    Long memberCount,
    Long programProblemCount,
    GroupRole role
) {

}
