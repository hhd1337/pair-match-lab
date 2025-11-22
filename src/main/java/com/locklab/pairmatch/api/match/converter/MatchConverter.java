package com.locklab.pairmatch.api.match.converter;

import com.locklab.pairmatch.api.match.dto.MatchResponseDTO;
import com.locklab.pairmatch.entity.Crew;
import com.locklab.pairmatch.entity.Mission;
import com.locklab.pairmatch.entity.PairGroup;
import com.locklab.pairmatch.entity.PairMember;
import com.locklab.pairmatch.service.match.MatchResult;
import java.util.List;
import java.util.stream.Collectors;

public class MatchConverter {

    public static MatchResponseDTO.MatchResultDTO toMatchResultDTO(MatchResult matchResult) {
        Mission mission = matchResult.getMission();
        List<PairGroup> pairGroups = matchResult.getPairGroups();

        List<MatchResponseDTO.PairGroupDTO> groupDTOs = pairGroups.stream()
                .map(MatchConverter::toPairGroupDTO)
                .collect(Collectors.toList());

        return MatchResponseDTO.MatchResultDTO.builder()
                .missionId(mission.getId())
                .missionName(mission.getName())
                .level(mission.getLevel())
                .missionOrderInLevel(mission.getMissionOrderInLevel())
                .groups(groupDTOs)
                .build();
    }

    private static MatchResponseDTO.PairGroupDTO toPairGroupDTO(PairGroup pairGroup) {
        List<MatchResponseDTO.CrewInGroupDTO> crewDTOs = pairGroup.getMembers().stream()
                .map(PairMember::getCrew)
                .map(MatchConverter::toCrewInGroupDTO)
                .collect(Collectors.toList());

        return MatchResponseDTO.PairGroupDTO.builder()
                .groupId(pairGroup.getId())
                .crews(crewDTOs)
                .build();
    }

    private static MatchResponseDTO.CrewInGroupDTO toCrewInGroupDTO(Crew crew) {
        return MatchResponseDTO.CrewInGroupDTO.builder()
                .crewId(crew.getId())
                .name(crew.getName())
                .build();
    }
}
