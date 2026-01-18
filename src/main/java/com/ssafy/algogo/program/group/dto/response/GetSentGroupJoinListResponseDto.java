package com.ssafy.algogo.program.group.dto.response;

import java.util.List;

public record GetSentGroupJoinListResponseDto(
    List<GetSentGroupJoinResponseDto> joins
) {

}
