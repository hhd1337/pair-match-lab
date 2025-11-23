package com.locklab.pairmatch.lock;

import com.locklab.pairmatch.service.match.MatchResult;

public interface LockingStrategy {

    MatchResult match(Long missionId);
}
