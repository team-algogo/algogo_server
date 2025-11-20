package com.ssafy.algogo.program.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupRoomResponseDto(
    Long programId,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    Long capacity,
    Long memberCount
) { }
