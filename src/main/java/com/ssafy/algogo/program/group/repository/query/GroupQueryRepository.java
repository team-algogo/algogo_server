package com.ssafy.algogo.program.group.repository.query;


import com.ssafy.algogo.program.group.dto.response.GroupRoomResponseDto;
import com.ssafy.algogo.program.group.dto.response.MyGroupRoomResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupQueryRepository {

    Page<GroupRoomResponseDto> findAllGroupRooms(String keyword, Pageable pageable);

    GroupRoomResponseDto getGroupRoomDetail(Long programId);

    Page<MyGroupRoomResponseDto> findMyGroupRooms(List<Long> programIds, Long userId,
        Pageable pageable);

    Page<GroupRoomResponseDto> findAllGroupRoomsWithMemberFlag(String keyword, Pageable pageable,
        Long userId);

    GroupRoomResponseDto getGroupRoomDetailWithUser(Long programId, Long userId);
}
