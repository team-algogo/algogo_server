package com.ssafy.algogo.user.dto.response;

public record UserSimpleResponseDto(
    Long userId,
    String profileImage,
    String nickname
) {

}