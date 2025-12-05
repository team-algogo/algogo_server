package com.ssafy.algogo.alarm.dto.response;


import com.ssafy.algogo.alarm.entity.Alarm;
import com.ssafy.algogo.alarm.entity.AlarmPayload;

public record AlarmResponseDto(
    Long id,
    String type,
    AlarmPayload payload,
    String message,
    Boolean isRead,
    String createdAt
) {

    public static AlarmResponseDto from(Alarm alarm) {
        return new AlarmResponseDto(
            alarm.getId(),
            alarm.getAlarmType().getName(),
            alarm.getPayload(),
            alarm.getMessage(),
            alarm.getIsRead(),
            alarm.getCreatedAt().toString()
        );
    }
}
