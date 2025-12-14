package com.ssafy.algogo.program.group.dto.response;

import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import java.util.List;
import org.springframework.data.domain.Page;

public record GroupRoomPageResponseDto(
    PageInfo page,
    SortInfo sort,
    boolean isUser,
    List<GroupRoomResponseDto> groupLists
) {

    public static GroupRoomPageResponseDto from(Page<GroupRoomResponseDto> groupRoomResponseDto,
        boolean isUser) {
        return new GroupRoomPageResponseDto(
            PageInfo.of(groupRoomResponseDto),
            SortInfo.of(groupRoomResponseDto),
            isUser,
            groupRoomResponseDto.getContent()
        );
    }
}