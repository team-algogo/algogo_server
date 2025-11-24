package com.ssafy.algogo.auth.dto.response;

import java.time.LocalDateTime;

public record LocalLoginResponseDto(
        Long userId,
        String description,
        String nickname,
        String profileImage,
        LocalDateTime createAt,
        LocalDateTime modifiedAt
) {
}
