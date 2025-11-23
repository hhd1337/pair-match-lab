package com.locklab.pairmatch.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockingStrategyRouter {

    private final NoneLockingStrategy noneLockingStrategy;
    private final LocalLockingStrategy localLockingStrategy;
    private final DbLockingStrategy dbLockingStrategy;
    private final RedisLockingStrategy redisLockingStrategy;

    public LockingStrategy resolve(LockType lockType) {
        if (lockType == null) {
            return noneLockingStrategy;
        }

        return switch (lockType) {
            case NONE -> noneLockingStrategy;
            case LOCAL -> localLockingStrategy;
            case DB -> dbLockingStrategy;
            case REDIS -> redisLockingStrategy;
        };
    }
}
