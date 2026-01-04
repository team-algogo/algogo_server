package com.ssafy.algogo.alarm.controller;

import com.ssafy.algogo.alarm.dto.request.DeleteAlarmRequestDto;
import com.ssafy.algogo.alarm.dto.response.GetAlarmListResponseDto;
import com.ssafy.algogo.alarm.dto.response.GetAlarmResponseDto;
import com.ssafy.algogo.alarm.dto.response.GetUnreadAlarmCountResponseDto;
import com.ssafy.algogo.alarm.entity.AlarmPayload;
import com.ssafy.algogo.auth.service.security.CustomUserDetails;
import com.ssafy.algogo.alarm.service.AlarmService;
import com.ssafy.algogo.common.advice.SuccessResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    /**
     * 구독을 시작한다는 요청이 들어오면 SeeEmitter를 반환해줌
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId
    ) {
        Long userId = customUserDetails.getUserId();
        return alarmService.subscribe(userId, lastEventId);
    }

    // test용 메서드
    @PostMapping("/test")
    public void testAlarm(@AuthenticationPrincipal CustomUserDetails user) {

        Long userId = user.getUserId();

        // 테스트용 페이로드 (임의 데이터)
        AlarmPayload payload = new AlarmPayload(
            999L,   // submissionId
            555L,   // reviewId
            null,   // programProblemId
            null,   // programId
            null,   // userId
            null,   // userNickname
            null,   // programName
            null,   // problemTitle
            null,   // joinId
            null,   // inviteId
            null,   // targetSubmissionAuthorNickname
            null,   // parentReviewId
            null,   // parentUserId
            null    // parentUserName
        );

        alarmService.createAndSendAlarm(
            userId,
            "TEST",  // alarm_type.name 에 TEST 가 존재해야 함
            payload,
            "테스트 알람이 도착했습니다!"
        );
    }

    @GetMapping("/lists")
    public SuccessResponse getMyAlarms(
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        GetAlarmListResponseDto getAlarmListResponseDto = alarmService.getMyAlarms(
            customUserDetails.getUserId());
        return new SuccessResponse("알림 리스트 조회를 성공했습니다.", getAlarmListResponseDto);
    }

    @GetMapping("/counts")
    public SuccessResponse getUnreadAlarmCount(
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        GetUnreadAlarmCountResponseDto getUnreadAlarmCountResponseDto = alarmService.getUnreadAlarmCount(
            customUserDetails.getUserId());
        return new SuccessResponse("알림 배지 조회를 성공했습니다.", getUnreadAlarmCountResponseDto
        );
    }

    @DeleteMapping
    public SuccessResponse deleteAlarms(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @RequestBody DeleteAlarmRequestDto request
    ) {
        alarmService.deleteAlarms(customUserDetails.getUserId(), request.getAlarmIds());

        return new SuccessResponse("알림 삭제를 성공했습니다.", null);
    }

    
}
