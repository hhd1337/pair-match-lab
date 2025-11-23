package com.locklab.pairmatch.repository;

import com.locklab.pairmatch.entity.Crew;
import com.locklab.pairmatch.entity.PairHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PairHistoryRepository extends JpaRepository<PairHistory, Long> {
    boolean existsByLevelAndCrew1AndCrew2(Integer level, Crew crew1, Crew crew2);

    boolean existsByLevelAndCrew1IdInAndCrew2IdIn(Integer level, List<Long> crew1Ids, List<Long> crew2Ids);
}
