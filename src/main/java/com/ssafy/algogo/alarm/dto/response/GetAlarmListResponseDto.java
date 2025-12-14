package com.ssafy.algogo.alarm.dto.response;

import java.util.List;

public record GetAlarmListResponseDto(
    List<GetAlarmResponseDto> alarms
) {

}
