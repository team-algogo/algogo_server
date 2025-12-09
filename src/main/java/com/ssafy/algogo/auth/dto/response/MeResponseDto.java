package com.ssafy.algogo.auth.dto.response;

import com.ssafy.algogo.user.entity.User;

import java.time.LocalDateTime;

public record MeResponseDto(
        Long userId,
        String email,
        String description,
        String nickname,
        String profileImage,
        LocalDateTime createAt,
        LocalDateTime modifiedAt
) {
    public static MeResponseDto from(User user) {
        return new MeResponseDto(
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
