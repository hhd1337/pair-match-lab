package com.locklab.pairmatch.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockingStrategyFactory {

    private final NoneLockingStrategy noneLockingStrategy;
    private final LocalLockingStrategy localLockingStrategy;
    private final DbLockingStrategy dbLockingStrategy;
    private final RedisLockingStrategy redisLockingStrategy;
    private final NamedLockingStrategy namedLockingStrategy;

    public LockingStrategy resolve(LockType lockType) {
        if (lockType == null) {
            return noneLockingStrategy;
        }

        return switch (lockType) {
            case NONE -> noneLockingStrategy;
            case JAVA_LOCAL -> localLockingStrategy;
            case DB_PESSIMISTIC -> dbLockingStrategy;
            case MYSQL_NAMED -> namedLockingStrategy;
            case REDIS -> redisLockingStrategy;
        };
    }
}
