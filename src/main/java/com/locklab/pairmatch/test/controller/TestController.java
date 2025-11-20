package com.locklab.pairmatch.test.controller;

import com.locklab.pairmatch.common.response.ApiResponse;
import com.locklab.pairmatch.test.converter.TestConverter;
import com.locklab.pairmatch.test.dto.TestResponseDTO.TestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test", description = "테스트용 API")
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    @Operation(
            summary = "응답 포맷 통일 테스트",
            description = "응답 본문이 isSuccess, code, message, result 구조로 일관되게 반환되는지 확인하기 위한 테스트용 API입니다."
    )
    @GetMapping("")
    public ApiResponse<TestDTO> test() {
        return ApiResponse.onSuccess(TestConverter.toTempTestDTO());
    }
}
