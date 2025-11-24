package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.common.exception.GeneralException;
import com.locklab.pairmatch.common.exception.status.ErrorStatus;
import com.locklab.pairmatch.repository.RedisLockRepository;
import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockingStrategy implements LockingStrategy {

    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final long WAIT_TIMEOUT_MILLISECOND = 5000L;
    private static final long RETRY_INTERVAL_MILLISECOND = 50L;

    private final PairMatchService pairMatchService;
    private final RedisLockRepository redisLockRepository;

    @Override
    public MatchResult match(Long missionId) {

        String ownerId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        String key = "lock:pair-match:mission:" + missionId;

        log.info("[RedisLock][BEGIN] 요청 시작 - key={} missionId={} owner={} thread={}", key, missionId, ownerId,
                Thread.currentThread().getName());

        try {
            // 락 획득 단계
            while (true) {
                boolean locked = redisLockRepository.acquireLock(missionId, ownerId, LOCK_TTL);

                if (locked) {
                    log.info("[RedisLock][ACQUIRED] 락 획득 성공 - missionId={} owner={}", missionId, ownerId);
                    break;
                }

                // 타임아웃 체크
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > WAIT_TIMEOUT_MILLISECOND) {
                    log.warn("[RedisLock][TIMEOUT] 락 획득 실패 - missionId={} owner={} waited={}ms", missionId, ownerId,
                            elapsed);
                    throw new GeneralException(ErrorStatus.LOCK_TIMEOUT);
                }

                // 대기
                log.debug("[RedisLock][WAIT] 락 대기 중... - missionId={} owner={} sleep={}ms", missionId, ownerId,
                        RETRY_INTERVAL_MILLISECOND);

                try {
                    Thread.sleep(RETRY_INTERVAL_MILLISECOND);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[RedisLock][INTERRUPTED] 락 대기 중 인터럽트 - missionId={} owner={}", missionId, ownerId);
                    throw new GeneralException(ErrorStatus.LOCK_INTERRUPTED);
                }
            }

            // 임계영역
            log.info("[RedisLock][ENTER] 임계영역 진입 - missionId={} owner={}", missionId, ownerId);
            MatchResult result = pairMatchService.match(missionId);
            log.info("[RedisLock][EXIT] 임계영역 종료 - missionId={} owner={}", missionId, ownerId);

            return result;

        } finally {
            log.info("[RedisLock][RELEASE] 락 해제 절차 시작 - missionId={} owner={}", missionId, ownerId);
            redisLockRepository.releaseLock(missionId, ownerId);
            log.info("[RedisLock][END] 요청 종료 - missionId={} owner={}", missionId, ownerId);
        }
    }
}

