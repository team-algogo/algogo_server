package com.ssafy.algogo.submission.event;

import com.ssafy.algogo.submission.dto.ReviewRematchTargetQueryDto;
import java.util.List;

public record SubmissionRematchEvent(
    List<ReviewRematchTargetQueryDto> reviewRematchTargetQueryDtoList
) {

}
