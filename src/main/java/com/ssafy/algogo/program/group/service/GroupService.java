package com.ssafy.algogo.program.group.service;

import com.ssafy.algogo.program.group.dto.request.CheckGroupNameRequestDto;
import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.request.UpdateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.CheckGroupNameResponseDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;

public interface GroupService {
  public GroupRoomResponseDto getGroupRoomDetail(Long programId);

  public GroupRoomResponseDto createGroupRoom(Long userId, CreateGroupRoomRequestDto createGroupRoomRequestDto);

  public CheckGroupNameResponseDto checkGroupName(CheckGroupNameRequestDto checkGroupNameRequestDto);

  public GroupRoomResponseDto updateGroupRoom(Long programId, UpdateGroupRoomRequestDto updateGroupRoomRequestDto);

  void applyGroupJoin(Long userId, Long programId);
}
