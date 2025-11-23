package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoneLockingStrategy implements LockingStrategy {

    private final PairMatchService pairMatchService;

    @Override
    public MatchResult match(Long missionId) {
        // 아무 락도 걸지 않고 바로 매칭 로직 수행
        return pairMatchService.match(missionId);
    }
}
