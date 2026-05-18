package com.example.production.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final StringRedisTemplate redis;
    private static final String PREFIX = "online:user:";
    private static final long TTL = 90; // giây - client ping mỗi 60s

    public void userOnline(String email, String sessionId) {
        redis.opsForSet().add(PREFIX + email, sessionId);
        redis.expire(PREFIX + email, TTL, TimeUnit.SECONDS);
    }

    public void userOffline(String email, String sessionId) {
        redis.opsForSet().remove(PREFIX + email, sessionId);
        Long size = redis.opsForSet().size(PREFIX + email);
        if (size != null && size == 0) redis.delete(PREFIX + email);
    }

    public boolean isOnline(String email) {
        Long size = redis.opsForSet().size(PREFIX + email);
        return size != null && size > 0;
    }

    public Set<String> getAllOnlineUsers() {
        Set<String> keys = redis.keys(PREFIX + "*");
        if (keys == null || keys.isEmpty()) return Collections.emptySet();
        return keys.stream().map(k -> k.replace(PREFIX, "")).collect(Collectors.toSet());
    }
}
