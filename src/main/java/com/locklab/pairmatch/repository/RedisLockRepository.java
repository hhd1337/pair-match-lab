package com.locklab.pairmatch.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisLockRepository {

    private static final String LOCK_KEY_PREFIX = "lock:pair-match:mission:";

    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(Long missionId, String ownerId, Duration ttl) {
        String key = LOCK_KEY_PREFIX + missionId;
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, ownerId, ttl);
        return Boolean.TRUE.equals(result);
    }

    public void releaseLock(Long missionId, String ownerId) {
        String key = LOCK_KEY_PREFIX + missionId;
        String currentOwner = redisTemplate.opsForValue().get(key);

        // 내가 잡은 락일 때만 해제
        if (ownerId.equals(currentOwner)) {
            redisTemplate.delete(key);
        }
    }
}
