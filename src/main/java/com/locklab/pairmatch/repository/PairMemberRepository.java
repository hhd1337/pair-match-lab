package com.locklab.pairmatch.repository;

import com.locklab.pairmatch.entity.PairMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PairMemberRepository extends JpaRepository<PairMember, Long> {
}
