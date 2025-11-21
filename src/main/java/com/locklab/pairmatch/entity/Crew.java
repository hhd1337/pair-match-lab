package com.locklab.pairmatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
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
public class Crew extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name;

    @Column
    private Boolean matched;

    @OneToMany(mappedBy = "crew")
    private List<PairMember> pairMembers = new ArrayList<>();

    @OneToMany(mappedBy = "crew1")
    private List<PairHistory> historiesAsCrew1 = new ArrayList<>();

    @OneToMany(mappedBy = "crew2")
    private List<PairHistory> historiesAsCrew2 = new ArrayList<>();

}

