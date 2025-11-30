package com.ssafy.algogo.user.dto.response;

import java.util.List;

public record ListSearchUserResponseDto(
        List<SearchUserResponseDto> users
) {
}
