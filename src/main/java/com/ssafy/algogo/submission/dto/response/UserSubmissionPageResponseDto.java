package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import java.util.List;
import org.springframework.data.domain.Page;

public record UserSubmissionPageResponseDto(
    PageInfo page,
    SortInfo sort,
    List<UserSubmissionResponseDto> submissions
) {

    public static UserSubmissionPageResponseDto from(
        Page<UserSubmissionResponseDto> userSubmissionResponseDto) {
        return new UserSubmissionPageResponseDto(
            PageInfo.of(userSubmissionResponseDto),
            SortInfo.of(userSubmissionResponseDto),
            userSubmissionResponseDto.getContent()
        );
    }
}
