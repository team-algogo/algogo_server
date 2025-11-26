package com.ssafy.algogo.user.dto.response;

public record UpdateUserProfileImageResponseDto(
        String profileImage
) {
    public static UpdateUserProfileImageResponseDto from(String profileImage) {
        return new UpdateUserProfileImageResponseDto(
                profileImage
        );
    }
}
