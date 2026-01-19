package com.ssafy.algogo.program.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.program.entity.InviteStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetReceivedGroupInviteResponseDto(
    Long inviteId,
    InviteStatus inviteStatus,
    GroupRoomResponseDto groupRoom
) {

}
