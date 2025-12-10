package com.ssafy.algogo.submission.dto;

import com.ssafy.algogo.submission.entity.Submission;
import java.util.List;

public record ReviewCandidateQueryDto(
    Submission submission,
    List<Long> algorithmIdList,
    Long reviewCount,
    Long requireReviewCount
) {

}
