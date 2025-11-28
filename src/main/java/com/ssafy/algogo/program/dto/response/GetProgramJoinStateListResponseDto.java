package com.ssafy.algogo.program.dto.response;

import java.util.List;

public record GetProgramJoinStateListResponseDto(
    List<GetProgramJoinStateResponseDto> users
) {

}
