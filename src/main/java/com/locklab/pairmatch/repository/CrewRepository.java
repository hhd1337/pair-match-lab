package com.locklab.pairmatch.repository;

import com.locklab.pairmatch.entity.Crew;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRepository extends JpaRepository<Crew, Long> {
    List<Crew> findAllByMatchedFalse();
}

