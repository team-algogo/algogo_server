package com.ssafy.algogo.program.group.service;

import com.ssafy.algogo.problem.dto.request.ProgramProblemCreateRequestDto;
import com.ssafy.algogo.problem.dto.request.ProgramProblemDeleteRequestDto;
import com.ssafy.algogo.problem.dto.response.ProgramProblemPageResponseDto;
import com.ssafy.algogo.program.dto.request.ApplyProgramInviteRequestDto;
import com.ssafy.algogo.program.dto.response.GetProgramInviteStateListResponseDto;
import com.ssafy.algogo.program.dto.response.GetProgramJoinStateListResponseDto;
import com.ssafy.algogo.program.group.dto.request.CheckGroupNameRequestDto;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupInviteStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupJoinStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupMemberRoleRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.CheckGroupNameResponseDto;
import com.ssafy.algogo.program.group.dto.response.GetGroupMemberListResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomPageResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface GroupService {

    GroupRoomResponseDto getGroupRoomDetail(Long programId);

    GroupRoomResponseDto createGroupRoom(Long userId,
        CreateGroupRoomRequestDto createGroupRoomRequestDto);

    CheckGroupNameResponseDto checkGroupName(CheckGroupNameRequestDto checkGroupNameRequestDto);

    GroupRoomResponseDto updateGroupRoom(Long programId,
        UpdateGroupRoomRequestDto updateGroupRoomRequestDto);

    void applyGroupJoin(Long userId, Long programId);

    void updateGroupJoinState(Long userId, Long programId, Long joinId,
        UpdateGroupJoinStateRequestDto updateGroupJoinStateRequestDto);

    GetProgramJoinStateListResponseDto getGroupJoinState(Long programId);

    void applyGroupInvite(Long programId,
        ApplyProgramInviteRequestDto applyProgramInviteRequestDto);

    void updateGroupInviteState(Long userId, Long programId, Long inviteId,
        UpdateGroupInviteStateRequestDto updateGroupInviteStateRequestDto);

    void deleteGroupInvite(Long programId, Long inviteId);

    GetProgramInviteStateListResponseDto getGroupInviteState(Long programId);

    GetGroupMemberListResponseDto getGroupMember(Long programId);

    void updateGroupMemberRole(Long programId, Long programUserId,
        UpdateGroupMemberRoleRequestDto updateGroupMemberRoleRequestDto);

    void deleteGroupMember(Long userId, Long programId, Long programUserId);

    void addGroupProblem(Long programId,
        ProgramProblemCreateRequestDto programProblemCreateRequestDto);

    ProgramProblemPageResponseDto getAllGroupProblems(Long programId, Pageable pageable);

    void deleteGroupProblems(Long programId,
        ProgramProblemDeleteRequestDto programProblemDeleteRequestDto);

    void deleteGroupRoom(Long programId);

    GroupRoomPageResponseDto getGroupRoomList(String keyword, Pageable pageable);
}
