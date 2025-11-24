package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.entity.Crew;
import com.locklab.pairmatch.repository.CrewRepository;
import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbLockingStrategy implements LockingStrategy {

    private final CrewRepository crewRepository;
    private final PairMatchService pairMatchService;

    @Override
    @Transactional
    public MatchResult match(Long missionId) {

        log.info("[DB-LOCK][PESSIMISTIC] missionId={} 비매칭 크루 row 잠금 시도 (SELECT ... FOR UPDATE)", missionId);

        // 비매칭 크루 조회 + FOR UPDATE -> 여기서 실제로 row-level lock 발생
        long start = System.currentTimeMillis();
        List<Crew> lockedCrews = crewRepository.findAllUnmatchedForUpdate();
        long elapsed = System.currentTimeMillis() - start;

        log.info("[DB-LOCK][PESSIMISTIC] missionId={} 락 획득 완료 - lockedRows={} (queryTime={}ms, thread={})", missionId,
                lockedCrews.size(), elapsed, Thread.currentThread().getName());
        log.info("[DB-LOCK][PESSIMISTIC] missionId={} 매칭 로직 실행 시작", missionId);

        MatchResult result = pairMatchService.match(missionId);

        int groupCount = (result.getPairGroups() != null) ? result.getPairGroups().size() : 0;
        log.info("[DB-LOCK][PESSIMISTIC] missionId={} 매칭 로직 실행 종료 - pairGroups={}", missionId, groupCount);

        // 트랜잭션이 정상 종료되면 DB가 row lock 해제
        log.info("[DB-LOCK][PESSIMISTIC] missionId={} 트랜잭션 종료 예정 → 커밋 시 row 락 해제", missionId);

        return result;
    }
}
