package com.locklab.pairmatch.service.match;

import com.locklab.pairmatch.common.exception.GeneralException;
import com.locklab.pairmatch.common.exception.status.ErrorStatus;
import com.locklab.pairmatch.entity.Crew;
import com.locklab.pairmatch.entity.Mission;
import com.locklab.pairmatch.entity.PairGroup;
import com.locklab.pairmatch.entity.PairHistory;
import com.locklab.pairmatch.entity.PairMember;
import com.locklab.pairmatch.repository.CrewRepository;
import com.locklab.pairmatch.repository.MissionRepository;
import com.locklab.pairmatch.repository.PairGroupRepository;
import com.locklab.pairmatch.repository.PairHistoryRepository;
import com.locklab.pairmatch.repository.PairMemberRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PairMatchServiceImpl implements PairMatchService {

    private final MissionRepository missionRepository;
    private final CrewRepository crewRepository;
    private final PairGroupRepository pairGroupRepository;
    private final PairMemberRepository pairMemberRepository;
    private final PairHistoryRepository pairHistoryRepository;

    @Override
    public MatchResult match(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MISSION_NOT_FOUND));

        List<Crew> candidates = new ArrayList<>(crewRepository.findAllByMatchedFalse());
        if (candidates.size() < 2) {
            log.warn("[PAIR_MATCH] missionId={} 매칭 실패 - 남은 후보자 수 부족 (remainingCandidates={})", missionId,
                    candidates.size());
            throw new GeneralException(ErrorStatus.MATCH_NOT_ENOUGH_CREW);
        }

        // 레이스 컨디션 확인용 인위적 지연
        try {
            Thread.sleep(3000); // 3초
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Collections.shuffle(candidates);

        List<PairGroup> createdGroups = new ArrayList<>();

        int index = 0;
        while (index < candidates.size() - 1) {
            int remaining = candidates.size() - index;
            int groupSize = decideGroupMemberNum(remaining);

            List<Crew> groupCrews = candidates.subList(index, index + groupSize);

            // 이 레벨에서 과거에 이미 만난 조합이 하나라도 있으면 에러
            if (hasAnyPreviousPairingInLevel(mission.getLevel(), groupCrews)) {
                throw new GeneralException(ErrorStatus.MATCH_DUPLICATED_PAIR_HISTORY);
            }

            PairGroup pairGroup = createPairGroup(mission);
            savePairMembers(pairGroup, groupCrews);
            savePairHistories(mission.getLevel(), groupCrews);

            createdGroups.add(pairGroup);
            index += groupSize;
        }

        return MatchResult.builder()
                .mission(mission)
                .pairGroups(createdGroups)
                .build();
    }

    private int decideGroupMemberNum(int remaining) {
        if (remaining == 3) {
            return 3;
        }
        return 2;
    }

    private PairGroup createPairGroup(Mission mission) {
        PairGroup pairGroup = PairGroup.builder()
                .mission(mission)
                .build();
        return pairGroupRepository.save(pairGroup);
    }

    private void savePairMembers(PairGroup pairGroup, List<Crew> groupCrews) {
        for (Crew crew : groupCrews) {
            PairMember pairMember = PairMember.builder()
                    .pairGroup(pairGroup)
                    .crew(crew)
                    .build();
            pairMemberRepository.save(pairMember);
            pairGroup.addMember(pairMember);

            crew.markMatched();
        }
    }

    private void savePairHistories(Integer level, List<Crew> groupCrews) {
        List<PairHistory> histories = new ArrayList<>();

        for (int i = 0; i < groupCrews.size(); i++) {
            for (int j = i + 1; j < groupCrews.size(); j++) {
                Crew first = groupCrews.get(i);
                Crew second = groupCrews.get(j);

                Crew crew1 = first;
                Crew crew2 = second;
                if (first.getId() > second.getId()) {
                    crew1 = second;
                    crew2 = first;
                }

                PairHistory history = PairHistory.builder()
                        .level(level)
                        .crew1(crew1)
                        .crew2(crew2)
                        .build();

                histories.add(history);
            }
        }

        pairHistoryRepository.saveAll(histories);
    }

    private boolean hasAnyPreviousPairingInLevel(Integer level, List<Crew> groupCrews) {
        List<Long> crewIds = groupCrews.stream()
                .map(Crew::getId)
                .toList();

        return pairHistoryRepository
                .existsByLevelAndCrew1IdInAndCrew2IdIn(level, crewIds, crewIds);
    }

}
