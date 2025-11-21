package com.locklab.pairmatch.repository;

import com.locklab.pairmatch.entity.PairHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PairHistoryRepository extends JpaRepository<PairHistory, Long> {
}
