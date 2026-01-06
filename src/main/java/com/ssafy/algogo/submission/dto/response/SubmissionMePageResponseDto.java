package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import java.util.List;
import org.springframework.data.domain.Page;

public record SubmissionMePageResponseDto(
    PageInfo page,
    SortInfo sort,
    List<SubmissionMeResponseDto> submissions
) {

    public static SubmissionMePageResponseDto from(
        Page<SubmissionMeResponseDto> userSubmissionResponseDto) {
        return new SubmissionMePageResponseDto(
            PageInfo.of(userSubmissionResponseDto),
            SortInfo.of(userSubmissionResponseDto),
            userSubmissionResponseDto.getContent()
        );
    }
}
