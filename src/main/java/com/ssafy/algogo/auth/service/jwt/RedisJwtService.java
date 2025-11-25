package com.ssafy.algogo.auth.service.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisJwtService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refreshToken:";
    private static final String TOKEN_KEY = "token";
    private static final String IP_KEY = "ip";
    private static final long REFRESH_TOKEN_VALID = 7;

    public void save(Long userId, String token, String ip) {
        String key = PREFIX + userId;
        redisTemplate.opsForHash().put(key, TOKEN_KEY, token);
        redisTemplate.opsForHash().put(key, IP_KEY, ip);
        redisTemplate.expire(key, REFRESH_TOKEN_VALID, TimeUnit.DAYS);
    }

    public Optional<String> findToken(Long userId) {
        String key = PREFIX + userId;
        Object token = redisTemplate.opsForHash().get(key, TOKEN_KEY);
        return Optional.ofNullable(token).map(Object::toString);
    }

    public Optional<String> findIp(Long userId) {
        String key = PREFIX + userId;
        Object ip = redisTemplate.opsForHash().get(key, IP_KEY);
        return Optional.ofNullable(ip).map(Object::toString);
    }

    public boolean validate(Long userId, String token, String ip) {
        return findToken(userId)
                .filter(savedToken -> savedToken.equals(token))
                .flatMap(t -> findIp(userId))
                .filter(savedIp -> savedIp.equals(ip))
                .isPresent();
    }

    public void delete(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }

    public boolean exists(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + userId));
    }


}
