package com.ssafy.algogo.review.dto.response;

import com.ssafy.algogo.submission.dto.response.AlgorithmResponseDto;
import java.time.LocalDateTime;
import java.util.Set;

public record CodeReviewSubmissionInfoDto(
    Long targetSubmissionId,
    String language,
    LocalDateTime createAt,
    Long viewCount,
    Set<AlgorithmResponseDto> algorithmList,
    Long reviewCount,
    String nickname
) {

}
