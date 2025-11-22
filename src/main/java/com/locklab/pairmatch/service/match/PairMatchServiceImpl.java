package com.locklab.pairmatch.service.match;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PairMatchServiceImpl implements PairMatchService {

    @Override
    @Transactional
    public MatchResult match(Long missionId) {
        // TODO: 실제 매칭 로직 구현
        return null;
    }
}
