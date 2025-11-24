package com.locklab.pairmatch.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisLockRepository {

    private static final String LOCK_KEY_PREFIX = "lock:pair-match:mission:";

    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(Long missionId, String ownerId, Duration ttl) {
        String key = LOCK_KEY_PREFIX + missionId;

        log.info("[RedisLock][TRY] SETNX 시도 - key={} owner={} ttl={}s", key, ownerId, ttl.getSeconds());

        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, ownerId, ttl);

        if (Boolean.TRUE.equals(result)) {
            log.info("[RedisLock][SUCCESS] 락 획득 성공 - key={} owner={}", key, ownerId);
        } else {
            log.info("[RedisLock][FAIL] 락 이미 선점됨 - key={} owner={} (currentOwner={})", key, ownerId,
                    redisTemplate.opsForValue().get(key));
        }

        return Boolean.TRUE.equals(result);
    }

    public void releaseLock(Long missionId, String ownerId) {
        String key = LOCK_KEY_PREFIX + missionId;

        log.info("[RedisLock][RELEASE-TRY] 락 해제 시도 - key={} owner={}", key, ownerId);

        String currentOwner = redisTemplate.opsForValue().get(key);

        if (currentOwner == null) {
            log.warn("[RedisLock][RELEASE-SKIP] 락 없음 - key={} (이미 만료 또는 삭제됨)", key);
            return;
        }

        if (!ownerId.equals(currentOwner)) {
            log.warn("[RedisLock][RELEASE-DENIED] owner 불일치 - key={} owner={} currentOwner={}", key, ownerId,
                    currentOwner);
            return;
        }

        redisTemplate.delete(key);
        log.info("[RedisLock][RELEASE-DONE] 락 해제 완료 - key={} owner={}", key, ownerId);
    }
}

