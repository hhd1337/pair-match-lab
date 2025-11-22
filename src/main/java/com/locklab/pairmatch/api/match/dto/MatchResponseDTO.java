package com.locklab.pairmatch.api.match.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MatchResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchResultDTO { // 매칭 결과 전체 DTO
        private Long missionId;
        private String missionName;
        private Integer level;
        private Integer missionOrderInLevel;

        private List<PairGroupDTO> groups;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PairGroupDTO { // 한 개 페어 그룹 (2~3인)
        private Long groupId;
        private List<CrewInGroupDTO> crews;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrewInGroupDTO { // 그룹 안에 들어가는 각 크루 정보
        private Long crewId;
        private String name;
    }
}
