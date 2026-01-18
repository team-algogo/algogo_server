package com.ssafy.algogo.program.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.program.entity.JoinStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetSentGroupJoinResponseDto(
    Long joinId,
    JoinStatus joinStatus,
    GroupRoomResponseDto groupRoom
) {

}
