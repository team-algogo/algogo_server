package com.ssafy.algogo.submission.dto.response;

import com.ssafy.algogo.common.dto.PageInfo;
import com.ssafy.algogo.common.dto.SortInfo;
import java.util.List;
import org.springframework.data.domain.Page;

public record SubmissionStatsPageResponseDto(
    PageInfo page,
    SortInfo sort,
    List<SubmissionStatsResponseDto> submissions
) {

    public static SubmissionStatsPageResponseDto from(
        Page<SubmissionStatsResponseDto> submissionStatsResponses) {
        return new SubmissionStatsPageResponseDto(
            PageInfo.of(submissionStatsResponses),
            SortInfo.of(submissionStatsResponses),
            submissionStatsResponses.getContent()
        );
    }
}
