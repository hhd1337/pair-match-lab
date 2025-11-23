package com.locklab.pairmatch.common.exception.status;

import com.locklab.pairmatch.common.exception.dto.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorStatus {
    // 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    _NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "해당 리소스를 찾을 수 없습니다."),

    // For test
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "테스트용 에러메세지입니다."),

    // 미션 관련 에러
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION4041", "요청하신 미션을 찾을 수 없습니다."),

    // 매칭 관련 에러
    MATCH_NOT_ENOUGH_CREW(HttpStatus.BAD_REQUEST, "MATCH4001", "매칭을 수행하기 위한 크루 수가 2명 미만입니다."),
    MATCH_DUPLICATED_PAIR_HISTORY(HttpStatus.CONFLICT, "MATCH4091", "해당 레벨에서 이미 페어로 매칭된 이력이 있는 조합이 포함되어 있습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
