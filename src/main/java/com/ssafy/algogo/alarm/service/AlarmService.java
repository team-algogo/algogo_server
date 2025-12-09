package com.ssafy.algogo.alarm.service;

import com.ssafy.algogo.alarm.dto.response.AlarmResponseDto;
import com.ssafy.algogo.alarm.entity.Alarm;
import com.ssafy.algogo.alarm.entity.AlarmPayload;
import com.ssafy.algogo.alarm.entity.AlarmType;
import com.ssafy.algogo.alarm.repository.AlarmRepository;
import com.ssafy.algogo.alarm.repository.AlarmTypeRepository;
import com.ssafy.algogo.alarm.repository.SseEmitterRepository;
import com.ssafy.algogo.common.advice.CustomException;
import com.ssafy.algogo.common.advice.ErrorCode;
import com.ssafy.algogo.user.entity.User;
import com.ssafy.algogo.user.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private static final Long DEFAULT_TIMEOUT = 60L * 60 * 1000L; // 1시간
    // 1시간동안 서버가 클라이언트에게 요청을 안 보내면 emitter의 타임아웃 이벤트가 발생하면서 종료됨
    // 이 경우 클라이언트가 새로 subscribe를 하게 할 지, Heartbeat(keep-alive)를 해서 무제한 유지할 지를 고민

    private final AlarmRepository alarmRepository;
    private final AlarmTypeRepository alarmTypeRepository;
    private final UserRepository userRepository;
    private final SseEmitterRepository emitterRepository;

    public SseEmitter subscribe(Long userId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 1) emitter 저장
        emitterRepository.add(userId, emitter);

        // 2) 타임아웃/완료/에러 처리를 등록해서 emitter 정리
        emitter.onCompletion(() -> {
//            log.info("SSE completed: userId={}", userId);
            emitterRepository.removeEmitter(userId, emitter);
        });

        emitter.onTimeout(() -> {
//            log.info("SSE timeout: userId={}", userId);
            emitter.complete();
            emitterRepository.removeEmitter(userId, emitter);
        });

        emitter.onError(e -> {
            log.warn("SSE error: userId={} e={}", userId, e.toString());
            emitter.completeWithError(e);
            emitterRepository.removeEmitter(userId, emitter);
        });

        // 503 에러 방지를 위해 처음 더미데이터 전송(처음 등록했을 때, 유효시간 내 전송되는 이벤트가 아예 없으면 503에러가 떠버려서 이거 방지)
        sendToEmitter(
            emitter,
            "INIT",
            new AlarmResponseDto(
                0L,
                "SYSTEM",
                null,
                "SSE connected",
                false,
                LocalDateTime.now().toString()
            )
        );

        return emitter;
    }

    // 다른 서비스에서 호출되는 메서드: 알람 생성 + DB 저장 + SSE 발송
    @Transactional // 음 이거 reqired_new로 따로가져가야 되나. 지금대로면 비즈니스로직은 성공했는데 여기서 실패하면 다 롤백될 거 같긴 한데..
    public void createAndSendAlarm(
        Long userId,
        String alarmTypeName,
        AlarmPayload payload,
        String message
    ) {

        AlarmType alarmType = alarmTypeRepository.findByName(alarmTypeName)
            .orElseThrow(
                () -> new CustomException("해당 이름에 해당하는 알람 타입이 없습니다", ErrorCode.ALARM_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("userId에 해당하는 데이터가 DB에 없습니다.",
                ErrorCode.USER_NOT_FOUND));

        Alarm alarm = Alarm.builder()
            .alarmType(alarmType)
            .payload(payload)
            .message(message)
            .isRead(false)
            .user(user)
            .build();

        alarmRepository.save(alarm);

        sendNotification(userId, AlarmResponseDto.from(alarm));
    }

    @Transactional
    public void deleteAlarmAndSend(Long alarmId) {
        alarmRepository.deleteById(alarmId);
    }

    /**
     * 특정 유저의 모든 SSE 연결에 알람 전송
     */
    public void sendNotification(Long userId, AlarmResponseDto dto) {

        List<SseEmitter> emitters = emitterRepository.get(userId);
        if (emitters.isEmpty()) {
//            log.info("No SSE connection for userId={}", userId);
            return;
        }

        Iterator<SseEmitter> iterator = emitters.iterator();

        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();

            try {
                sendToEmitter(emitter, "NOTIFICATION",
                    dto); // 프론트는 NOTIFICATION이라는 이름으로 이벤트 리스너를 등록
            } catch (Exception e) {
                log.warn("SSE send failed, removing emitter. userId={}, error={}", userId,
                    e.toString());
                emitter.complete();
                iterator.remove();
            }
        }
    }

    private void sendToEmitter(SseEmitter emitter, String eventName, AlarmResponseDto data) {
        try {
            emitter.send(
                SseEmitter.event()
                    .id(String.valueOf(data.id()))
                    .name(eventName)
                    .data(data)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
