package com.ssafy.algogo.alarm.entity;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlarmPayload(
    Long submissionId,
    Long reviewId,
    Long programProblemId,
    Long programId,
    Long userId,
    String userNickname,
    String programName,
    String problemTitle,
    Long joinId,
    Long inviteId,
    String targetSubmissionAuthorNickname,
    Long parentReviewId,
    Long parentUserId,
    String parentUserName
) {

}
