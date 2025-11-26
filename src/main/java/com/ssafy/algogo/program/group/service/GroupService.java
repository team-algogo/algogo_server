package com.ssafy.algogo.program.group.service;

import com.ssafy.algogo.program.dto.request.ApplyProgramInviteRequestDto;
import com.ssafy.algogo.program.dto.response.GetGroupJoinStateListResponseDto;
import com.ssafy.algogo.program.group.dto.request.CheckGroupNameRequestDto;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupInviteStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupJoinStateRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.CheckGroupNameResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;

public interface GroupService {
  GroupRoomResponseDto getGroupRoomDetail(Long programId);

  GroupRoomResponseDto createGroupRoom(Long userId, CreateGroupRoomRequestDto createGroupRoomRequestDto);

  CheckGroupNameResponseDto checkGroupName(CheckGroupNameRequestDto checkGroupNameRequestDto);

  GroupRoomResponseDto updateGroupRoom(Long programId, UpdateGroupRoomRequestDto updateGroupRoomRequestDto);

  void applyGroupJoin(Long userId, Long programId);

  void updateGroupJoinState(Long userId, Long programId, Long joinId, UpdateGroupJoinStateRequestDto updateGroupJoinStateRequestDto);

    GetGroupJoinStateListResponseDto getGroupJoinState(Long programId);

  void applyGroupInvite(Long programId, ApplyProgramInviteRequestDto applyProgramInviteRequestDto);

  void updateGroupInviteState(Long userId, Long programId, Long inviteId, UpdateGroupInviteStateRequestDto updateGroupInviteStateRequestDto);

  void deleteGroupInvite(Long programId, Long inviteId);
}
