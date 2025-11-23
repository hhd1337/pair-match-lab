package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.entity.Crew;
import com.locklab.pairmatch.repository.CrewRepository;
import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbLockingStrategy implements LockingStrategy {

    private final CrewRepository crewRepository;
    private final PairMatchService pairMatchService;

    @Override
    @Transactional
    public MatchResult match(Long missionId) {

        // 1) 비관적 락으로 크루 목록을 읽음 → 여기서 락이 걸림
        List<Crew> lockedCrews = crewRepository.findAllUnmatchedForUpdate();

        // 2) 이 시점부터 lockedCrews 는 다른 서버/스레드가 수정 불가
        // 3) 이제 match() 실행해도 race condition 발생 불가
        return pairMatchService.match(missionId);
    }
}

