package com.ssafy.algogo.alarm.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterRepository {
    // 이건 HashMap에 SseEmitter를 저장하기 위한 리포지토리
    // 그니까 연결되어있는 SeeEmitter들을 저장해놓고, 여기다가 이벤트 스트림을 전송하기 위한 클래스

    // 알람 데이터 자체 저장은 spring data jpa를 활용
    // 기본적으로 RDB에 저장하고 실제 필요한 데이터는 payload로 관리
    // 동기처리나 전송 같은 건 redis나 kafka를 쓰는 것 같긴한데 좀 더 공부


    // 하나의 유저가 여러 개의 탭(페이지 여러 개, 앱이나 웹 등등 여러 경로로 서비스 접근 등)에서 구독 가능
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter add(Long userId, SseEmitter emitter) {
        emitters
            .computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()) // 스레드 고려
            .add(emitter);
        return emitter;
    }

    public List<SseEmitter> get(Long userId) {
        return emitters.getOrDefault(userId, List.of());
    }

    public void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }

        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

    public void removeAllEmittersForUser(Long userId) {
        emitters.remove(userId);
    }
}
