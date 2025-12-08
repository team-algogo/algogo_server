package com.ssafy.algogo.program.group.dto.response;

import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import java.util.List;
import org.springframework.data.domain.Page;

public record MyGroupRoomPageResponseDto(
    PageInfo page,
    SortInfo sort,
    List<MyGroupRoomResponseDto> groupLists
) {

    public static MyGroupRoomPageResponseDto from(Page<MyGroupRoomResponseDto> pageData) {
        return new MyGroupRoomPageResponseDto(
            PageInfo.of(pageData),
            SortInfo.of(pageData),
            pageData.getContent()
        );
    }
}
