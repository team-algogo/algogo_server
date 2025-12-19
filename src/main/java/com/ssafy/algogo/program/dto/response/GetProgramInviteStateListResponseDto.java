package com.ssafy.algogo.program.dto.response;

import java.util.List;

public record GetProgramInviteStateListResponseDto(
    List<GetProgramInviteStateResponseDto> users
) {

}
