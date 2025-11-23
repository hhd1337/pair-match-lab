package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.common.exception.GeneralException;
import com.locklab.pairmatch.common.exception.status.ErrorStatus;
import com.locklab.pairmatch.repository.NamedLockRepository;
import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NamedLockingStrategy implements LockingStrategy {

    private static final long LOCK_TIMEOUT_SECONDS = 5L;

    private final PairMatchService pairMatchService;
    private final NamedLockRepository namedLockRepository;

    @Override
    @Transactional
    public MatchResult match(Long missionId) {
        String lockName = "pair-match-mission-" + missionId; // missionId 별 다른 락 이름 사용

        System.out.println("[NamedLock] 락 획득 시도 - " + lockName + " / " + Thread.currentThread().getName());

        boolean acquired = namedLockRepository.acquireLock(lockName, LOCK_TIMEOUT_SECONDS);
        if (!acquired) {
            throw new GeneralException(ErrorStatus.MATCH_LOCK_ACQUIRE_FAILED);
        }

        try { // 네임드락 으로 보호되는 임계 영역

            System.out.println("[NamedLock] 락 획득 성공 - " + lockName + " / " + Thread.currentThread().getName());
            // 테스트용 딜레이 (락 오래 잡고 있도록)
            Thread.sleep(3000);

            return pairMatchService.match(missionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            namedLockRepository.releaseLock(lockName);
            System.out.println("[NamedLock] 락 해제 완료 - " + lockName + " / " + Thread.currentThread().getName());
        }
    }
}
