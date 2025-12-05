package com.ssafy.algogo.alarm.service;

import com.ssafy.algogo.alarm.repository.SseEmitterRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private static final Long DEFAULT_TIMEOUT = 60L * 60 * 1000L; // 1시간
    // 1시간동안 서버가 클라이언트에게 요청을 안 보내면 emitter의 타임아웃 이벤트가 발생하면서 종료됨
    // 이 경우 클라이언트가 새로 subscribe를 하게 할 지, Heartbeat(keep-alive)를 해서 무제한 유지할 지를 고민

    private final SseEmitterRepository emitterRepository;

//    public SseEmitter subscribe(Long userId, String lastEventId) {
//        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
//
//        // 1) emitter 저장
//        emitterRepository.add(userId, emitter);
//
//        // 2) 타임아웃/완료/에러 처리를 등록해서 emitter 정리
//        emitter.onTimeout(() -> {
//            log.info("SSE timeout: userId={}", userId);
//            emitter.complete();
//            emitterRepository.removeEmitter(userId, emitter);
//        });
//
//        emitter.onCompletion(() -> {
//            log.info("SSE completed: userId={}", userId);
//            emitterRepository.removeEmitter(userId, emitter);
//        });
//
//        emitter.onError(e -> {
//            log.warn("SSE error: userId={} e={}", userId, e.toString());
//            emitterRepository.removeEmitter(userId, emitter);
//        });
//
//        // 503 에러 방지를 위해 처음 더미데이터 전송(처음 등록했을 때, 유효시간 내 전송되는 이벤트가 아예 없으면 503에러가 떠버려서 이거 방지)
//        sendToEmitter(emitter, "INIT", AlarmDto.builder()
//            .id(0L)
//            .title("connected")
//            .message("SSE connected")
//            .type("SYSTEM")
//            .createdAt(now())
//            .build()
//        );
//
//        return emitter;
//    }
//
//    // 알림 발송 (다른 서비스에서 호출)
//    public void sendNotification(Long userId, AlarmDto notification) {
//        List<SseEmitter> emitters = emitterRepository.get(userId);
//        if (emitters.isEmpty()) {
//            log.info("No active SSE connection for userId={}", userId);
//            return;
//        }
//
//        Iterator<SseEmitter> iterator = emitters.iterator();
//        while (iterator.hasNext()) {
//            SseEmitter emitter = iterator.next();
//            try {
//                sendToEmitter(emitter, "notification", notification);
//            } catch (Exception e) {
//                // 적절한 에러 처리 추가
//                log.warn("Sending SSE failed, remove emitter. userId={}, err={}", userId,
//                    e.toString());
//                emitter.complete();
//                iterator.remove();
//            }
//        }
//    }
//
//    private void sendToEmitter(SseEmitter emitter, String eventName, AlarmDto data) {
//        try {
//            emitter.send(
//                SseEmitter.event()
//                    .id(String.valueOf(data.getId())) // Last-Event-ID 재연결에 사용 가능
//                    .name(eventName)
//                    .data(data)
//            );
//        } catch (IOException e) {
//            // 이것도 적절한 에러 처리 추가
//            throw new RuntimeException(e);
//        }
//    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
