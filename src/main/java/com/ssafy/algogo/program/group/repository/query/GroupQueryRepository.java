package com.ssafy.algogo.program.group.repository.query;


import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupQueryRepository {

    Page<GroupRoomResponseDto> findAllGroupRooms(String keyword, Pageable pageable);

    GroupRoomResponseDto getGroupRoomDetail(Long programId);
}
