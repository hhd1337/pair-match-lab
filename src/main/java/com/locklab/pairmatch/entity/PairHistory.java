package com.locklab.pairmatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "pair_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_PAIR_HISTORY_LEVEL_CREW1_CREW2",
                        columnNames = {"level", "crew1_id", "crew2_id"}
                )
        }
)
public class PairHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew1_id", nullable = false)
    private Crew crew1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew2_id", nullable = false)
    private Crew crew2;

}

