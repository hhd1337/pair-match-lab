package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbLockingStrategy implements LockingStrategy {

    private final PairMatchService pairMatchService;

    @Override
    public MatchResult match(Long missionId) {
        // TODO: PESSIMISTIC_WRITE 등의 DB 락 활용
        return pairMatchService.match(missionId);
    }
}
