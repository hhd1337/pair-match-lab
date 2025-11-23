package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockingStrategy implements LockingStrategy {

    private final PairMatchService pairMatchService;
    // private final RedissonClient redissonClient; // 나중에 Redis 붙일 때 주입

    @Override
    public MatchResult match(Long missionId) {
        // TODO: Redis 기반 분산락 구현 (SET NX, Redisson RLock 등)
        return pairMatchService.match(missionId);
    }
}
