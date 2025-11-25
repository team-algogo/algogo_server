package com.ssafy.algogo.auth.dto.response;

import com.ssafy.algogo.user.entity.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LocalLoginResponseDto(
        Long userId,
        String email,
        String description,
        String nickname,
        String profileImage,
        LocalDateTime createAt,
        LocalDateTime modifiedAt
) {
    public static LocalLoginResponseDto response(User user) {
        return new LocalLoginResponseDto(
                user.getId(),
                user.getEmail(),
                user.getDescription(),
                user.getNickname(),
                user.getProfileImage(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
