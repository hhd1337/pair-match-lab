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
        log.info("[PAIR_MATCH] missionId={} 매칭 시작", missionId);

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MISSION_NOT_FOUND));

        List<Crew> candidates = new ArrayList<>(crewRepository.findAllByMatchedFalse());
        log.info("[PAIR_MATCH] missionId={} 현재 비매칭 크루 수={}", missionId, candidates.size());

        if (candidates.size() < 2) {
            log.warn(
                    "[PAIR_MATCH] missionId={} 매칭 실패 - 남은 후보자 수 부족 (remainingCandidates={})",
                    missionId,
                    candidates.size()
            );
            throw new GeneralException(ErrorStatus.MATCH_NOT_ENOUGH_CREW);
        }

        // 레이스 컨디션 확인용 인위적 지연
        try {
            log.debug("[PAIR_MATCH] missionId={} 레이스 컨디션 테스트용 지연 시작 (3s)", missionId);
            Thread.sleep(3000); // 3초
            log.debug("[PAIR_MATCH] missionId={} 레이스 컨디션 테스트용 지연 종료", missionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[PAIR_MATCH] missionId={} 지연 중 인터럽트 발생", missionId, e);
        }

        Collections.shuffle(candidates);
        log.info(
                "[PAIR_MATCH] missionId={} 후보 크루 셔플 완료 - firstCandidates={}",
                missionId,
                candidates.stream().limit(5).map(Crew::getName).toList()
        );

        List<PairGroup> createdGroups = new ArrayList<>();

        int index = 0;
        int groupSeq = 1;
        while (index < candidates.size() - 1) {
            int remaining = candidates.size() - index;
            int groupSize = decideGroupMemberNum(remaining);

            List<Crew> groupCrews = candidates.subList(index, index + groupSize);

            log.info("[PAIR_MATCH] missionId={} 그룹 생성 시도 seq={} size={} crews={}", missionId, groupSeq, groupSize,
                    groupCrews.stream().map(Crew::getName).toList());

            if (hasAnyPreviousPairingInLevel(mission.getLevel(), groupCrews)) {
                log.warn("[PAIR_MATCH] missionId={} 그룹 seq={} 중복 페어 이력 발견 → 재매칭 필요 (level={}, crews={})", missionId,
                        groupSeq, mission.getLevel(), groupCrews.stream().map(Crew::getId).toList());
                throw new GeneralException(ErrorStatus.MATCH_DUPLICATED_PAIR_HISTORY);
            }

            PairGroup pairGroup = createPairGroup(mission);
            savePairMembers(pairGroup, groupCrews);
            savePairHistories(mission.getLevel(), groupCrews);

            log.info("[PAIR_MATCH] missionId={} 그룹 확정 seq={} pairGroupId={} crews={}", missionId, groupSeq,
                    pairGroup.getId(), groupCrews.stream().map(Crew::getId).toList());

            createdGroups.add(pairGroup);
            index += groupSize;
            groupSeq++;
        }

        log.info("[PAIR_MATCH] missionId={} 매칭 완료 - 총 그룹 수={}, 사용된 크루 수={}", missionId, createdGroups.size(),
                createdGroups.stream().mapToInt(g -> g.getMembers().size()).sum());

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
