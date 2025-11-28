package com.ssafy.algogo.program.group.repository.query;


import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;

public interface GroupQueryRepository {

    GroupRoomResponseDto getGroupRoomDetail(Long programId);
}
