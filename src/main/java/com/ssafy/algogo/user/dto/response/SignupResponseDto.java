package com.ssafy.algogo.user.dto.response;

import com.ssafy.algogo.user.entity.User;

import java.time.LocalDateTime;

public record SignupResponseDto(
        Long userId,
        String email,
        String description,
        String nickname,
        String profileImage,
        LocalDateTime createAt,
        LocalDateTime modifiedAt
) {
    public static SignupResponseDto from(User user) {
        return new SignupResponseDto(
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
