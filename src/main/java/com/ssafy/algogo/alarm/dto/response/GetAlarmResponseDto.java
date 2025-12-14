package com.ssafy.algogo.alarm.dto.response;


import com.ssafy.algogo.alarm.entity.Alarm;
import com.ssafy.algogo.alarm.entity.AlarmPayload;

public record GetAlarmResponseDto(
    Long id,
    String type,
    AlarmPayload payload,
    String message,
    Boolean isRead,
    String createdAt
) {

    public static GetAlarmResponseDto from(Alarm alarm) {
        return new GetAlarmResponseDto(
            alarm.getId(),
            alarm.getAlarmType().getName(),
            alarm.getPayload(),
            alarm.getMessage(),
            alarm.getIsRead(),
            alarm.getCreatedAt().toString()
        );
    }
}
