package com.locklab.pairmatch.api.match;

import com.locklab.pairmatch.api.match.converter.MatchConverter;
import com.locklab.pairmatch.api.match.dto.MatchResponseDTO;
import com.locklab.pairmatch.common.response.ApiResponse;
import com.locklab.pairmatch.lock.LockType;
import com.locklab.pairmatch.lock.LockingStrategy;
import com.locklab.pairmatch.lock.LockingStrategyRouter;
import com.locklab.pairmatch.service.match.MatchResult;
import com.locklab.pairmatch.service.match.PairMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Match", description = "페어 매칭 관련 API")
@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
@Validated
public class MatchController {

    private final PairMatchService pairMatchService;
    private final LockingStrategyRouter lockingStrategyRouter;


    @Operation(
            summary = "특정 미션에 대해 페어 매칭 1회 실행",
            description = """
                    주어진 미션 ID에 대해 페어 매칭을 1회 실행합니다.
                    """
    )
    @PostMapping("/{missionId}")
    public ApiResponse<MatchResponseDTO.MatchResultDTO> matchOnce(
            @PathVariable("missionId") @Positive Long missionId,
            @RequestParam(name = "lock", required = false, defaultValue = "NONE") LockType lockType
    ) {
        LockingStrategy strategy = lockingStrategyRouter.resolve(lockType);

        MatchResult matchResult = strategy.match(missionId);

        return ApiResponse.onSuccess(MatchConverter.toMatchResultDTO(matchResult));
    }

}
