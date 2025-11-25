package com.ssafy.algogo.program.group.service;

import com.ssafy.algogo.program.group.dto.request.CreateGroupRoomRequestDto;
import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;

public interface GroupService {
//  public GroupRoomResponseDto getGroupRoomDetail(Long programId);

  public GroupRoomResponseDto createGroupRoom(Long userId, CreateGroupRoomRequestDto createGroupRoomRequestDto);
}
