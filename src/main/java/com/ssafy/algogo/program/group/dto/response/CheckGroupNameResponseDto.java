package com.ssafy.algogo.program.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CheckGroupNameResponseDto(
    Boolean isAvailable
) {

}
