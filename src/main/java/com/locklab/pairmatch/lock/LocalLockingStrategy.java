package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalLockingStrategy implements LockingStrategy {

    private final PairMatchService pairMatchService;
    private final Object lock = new Object(); // 이 객체 하나에 대한 모니터락으로 동시성 제어

    @Override
    public MatchResult match(Long missionId) {
        synchronized (lock) {
            return pairMatchService.match(missionId);
        }
    }
}
