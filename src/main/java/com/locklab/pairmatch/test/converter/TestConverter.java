package com.locklab.pairmatch.test.converter;

import com.locklab.pairmatch.test.dto.TestResponseDTO;

public class TestConverter {
    public static TestResponseDTO.TestDTO toTempTestDTO() {
        return TestResponseDTO.TestDTO.builder()
                .testString("테스트 성공")
                .build();
    }

    public static TestResponseDTO.ExceptionDTO toExceptionDTO(Integer flag) {
        return TestResponseDTO.ExceptionDTO.builder()
                .flag(flag)
                .build();
    }
}
