package com.ssafy.algogo.program.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.program.entity.JoinStatus;
import com.ssafy.algogo.program.entity.ProgramJoin;
import com.ssafy.algogo.user.entity.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetProgramJoinStateResponseDto(
    Long joinId,
    String email,
    String profileImage,
    String nickname,
    JoinStatus status
) {

    public static GetProgramJoinStateResponseDto from(ProgramJoin programJoin) {
        User user = programJoin.getUser();
        return new GetProgramJoinStateResponseDto(
            programJoin.getId(),
            user.getEmail(),
            user.getProfileImage(),
            user.getNickname(),
            programJoin.getJoinStatus()
        );
    }
}
