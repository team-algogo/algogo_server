package com.ssafy.algogo.program.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.program.entity.InviteStatus;
import com.ssafy.algogo.program.entity.ProgramInvite;
import com.ssafy.algogo.user.entity.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetProgramInviteStateResponseDto(
    Long inviteId,
    String email,
    String profileImage,
    String nickname,
    InviteStatus status
) {
    public static GetProgramInviteStateResponseDto from(ProgramInvite programInvite) {
        User user = programInvite.getUser();
        return new GetProgramInviteStateResponseDto(
            programInvite.getId(),
            user.getEmail(),
            user.getProfileImage(),
            user.getNickname(),
            programInvite.getInviteStatus()
        );
    }
}
