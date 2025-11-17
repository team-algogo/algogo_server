package com.ssafy.algogo.alarm.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlarmPayload(Long submission_id, Long review_id, Long program_problem_id, Long program_id) {
}
