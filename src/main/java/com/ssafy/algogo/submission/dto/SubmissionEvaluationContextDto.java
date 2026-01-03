package com.ssafy.algogo.submission.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubmissionEvaluationContextDto {

    /**
     * Submission 기본 정보
     */
    private Long submissionId;
    private String code;
    private String language;
    private Long execTime;
    private Long memory;
    private String strategy;
    private Boolean isSuccess;

    /**
     * 사용된 알고리즘 ID 목록
     */
    private List<Long> algorithmIds;

    /**
     * 문제 정보
     */
    private Long problemId;
    private String problemTitle;
    private String difficultyType;
    private String problemLink;
}

