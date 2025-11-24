package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.common.exception.GeneralException;
import com.locklab.pairmatch.common.exception.status.ErrorStatus;
import com.locklab.pairmatch.repository.NamedLockRepository;
import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NamedLockingStrategy implements LockingStrategy {

    private static final long LOCK_TIMEOUT_SECONDS = 5L;

    private final PairMatchService pairMatchService;
    private final NamedLockRepository namedLockRepository;

    @Override
    @Transactional
    public MatchResult match(Long missionId) {
        String lockName = "pair-match-mission-" + missionId;

        log.info("[NamedLock] [TRY] 락 획득 시도 - name={} thread={}", lockName, Thread.currentThread().getName());

        boolean acquired = namedLockRepository.acquireLock(lockName, LOCK_TIMEOUT_SECONDS);

        if (!acquired) {
            log.warn("[NamedLock] [FAIL] 락 획득 실패 - name={} thread={}", lockName, Thread.currentThread().getName());
            throw new GeneralException(ErrorStatus.MATCH_LOCK_ACQUIRE_FAILED);
        }

        log.info("[NamedLock] [SUCCESS] 락 획득 - name={} thread={}", lockName, Thread.currentThread().getName());

        try {
            log.debug("[NamedLock] [HOLD] 임계구역 실행 준비 (3초 대기) - name={}", lockName);
            Thread.sleep(3000);

            return pairMatchService.match(missionId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[NamedLock] [INTERRUPTED] name={} error={}", lockName, e.getMessage());
            throw new RuntimeException(e);

        } finally {
            log.info("[NamedLock] [RELEASE] 락 해제 시도 - name={} thread={}", lockName, Thread.currentThread().getName());

            namedLockRepository.releaseLock(lockName);

            log.info("[NamedLock] [END] 락 해제 완료 - name={} thread={}", lockName, Thread.currentThread().getName());
        }
    }
}

