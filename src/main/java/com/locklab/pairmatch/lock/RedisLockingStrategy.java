package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.common.exception.GeneralException;
import com.locklab.pairmatch.common.exception.status.ErrorStatus;
import com.locklab.pairmatch.repository.RedisLockRepository;
import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockingStrategy implements LockingStrategy {

    private static final Duration LOCK_TTL = Duration.ofSeconds(5); // 락 수명
    private static final long WAIT_TIMEOUT_MILLISECOND = 5000L;         // 최대 대기 시간
    private static final long RETRY_INTERVAL_MILLISECOND = 50L;          // 재시도 간격

    private final PairMatchService pairMatchService;
    private final RedisLockRepository redisLockRepository;

    @Override
    public MatchResult match(Long missionId) {
        String ownerId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            // 락 획득 시도(스핀+백오프)
            while (!redisLockRepository.acquireLock(missionId, ownerId, LOCK_TTL)) {

                if (System.currentTimeMillis() - startTime > WAIT_TIMEOUT_MILLISECOND) {
                    throw new GeneralException(ErrorStatus.LOCK_TIMEOUT);
                }

                try {
                    Thread.sleep(RETRY_INTERVAL_MILLISECOND);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new GeneralException(ErrorStatus.LOCK_INTERRUPTED);
                }
            }

            // 락 잡은 뒤 실제 매칭로직 실행
            return pairMatchService.match(missionId);

        } finally {
            // 내가 잡은 락이면 해제
            redisLockRepository.releaseLock(missionId, ownerId);
        }
    }
}
