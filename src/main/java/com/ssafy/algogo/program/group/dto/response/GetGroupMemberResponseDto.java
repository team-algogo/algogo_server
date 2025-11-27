package com.ssafy.algogo.program.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.algogo.program.group.entity.GroupRole;
import com.ssafy.algogo.program.group.entity.GroupsUser;
import com.ssafy.algogo.user.entity.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetGroupMemberResponseDto(
    Long programUserId,
    String email,
    String profileImage,
    String nickname,
    GroupRole role
) {
    public static GetGroupMemberResponseDto from(GroupsUser groupsUser) {
        User user = groupsUser.getUser();
        return new GetGroupMemberResponseDto(
            groupsUser.getId(),
            user.getEmail(),
            user.getProfileImage(),
            user.getNickname(),
            groupsUser.getGroupRole()
        );
    }
}
