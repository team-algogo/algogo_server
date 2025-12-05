package com.ssafy.algogo.submission.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrendIdsResponseDto(
    List<Long> submissionIdList,
    List<Long> programProblemIdList
) {

}
