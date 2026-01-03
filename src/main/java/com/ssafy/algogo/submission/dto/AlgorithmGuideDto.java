package com.ssafy.algogo.submission.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * ElasticSearch에서 조회한 알고리즘 가이드 정보
 */
@Getter
@Builder
public class AlgorithmGuideDto {

    private Long algorithmId;
    private String algorithmName;
    private String scenarioId;
    private String scenarioTitle;
    private List<String> situationTags;
    private String guideText;
    private String pitfalls;
    private String scoringRubricHint;
}

