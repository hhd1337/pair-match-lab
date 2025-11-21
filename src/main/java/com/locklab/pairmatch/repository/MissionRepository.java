package com.locklab.pairmatch.repository;

import com.locklab.pairmatch.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
}

