package com.ssafy.algogo.user.dto.response;

import com.ssafy.algogo.user.entity.User;

import java.time.LocalDateTime;

public record UserInfoResponseDto(
        Long userId,
        String email,
        String description,
        String nickname,
        String profileImage,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static UserInfoResponseDto from(User user) {
        return new UserInfoResponseDto(
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
