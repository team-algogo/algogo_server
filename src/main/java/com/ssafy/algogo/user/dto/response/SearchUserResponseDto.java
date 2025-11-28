package com.ssafy.algogo.user.dto.response;

import com.ssafy.algogo.user.entity.User;

import java.time.LocalDateTime;

public record SearchUserResponseDto(
        Long userId,
        String email,
        String description,
        String nickname,
        String profileImage,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static SearchUserResponseDto from(User user) {
        return new SearchUserResponseDto(
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
