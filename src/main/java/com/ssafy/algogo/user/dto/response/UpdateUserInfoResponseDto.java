package com.ssafy.algogo.user.dto.response;

import com.ssafy.algogo.user.entity.User;

import java.time.LocalDateTime;

public record UpdateUserInfoResponseDto(
        Long userId,
        String email,
        String description,
        String nickname,
        String profileImage,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static UpdateUserInfoResponseDto from(User user) {
        return new UpdateUserInfoResponseDto(
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
