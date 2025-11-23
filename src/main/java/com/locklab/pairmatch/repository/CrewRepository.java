package com.locklab.pairmatch.repository;

import com.locklab.pairmatch.entity.Crew;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CrewRepository extends JpaRepository<Crew, Long> {
    List<Crew> findAllByMatchedFalse();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Crew c where c.matched = false")
    List<Crew> findAllUnmatchedForUpdate();
}

