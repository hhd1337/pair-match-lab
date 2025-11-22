package com.locklab.pairmatch.service.match;

import com.locklab.pairmatch.entity.Mission;
import com.locklab.pairmatch.entity.PairGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MatchResult {
    private final Mission mission;
    private final java.util.List<PairGroup> pairGroups;
}
