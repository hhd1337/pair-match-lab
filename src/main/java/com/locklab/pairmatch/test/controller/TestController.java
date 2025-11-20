package com.locklab.pairmatch.test.controller;

import com.locklab.pairmatch.common.response.ApiResponse;
import com.locklab.pairmatch.test.converter.TestConverter;
import com.locklab.pairmatch.test.dto.TestResponseDTO.TestDTO;
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
    @GetMapping("")
    public ApiResponse<TestDTO> test() {
        return ApiResponse.onSuccess(TestConverter.toTempTestDTO());
    }

}
