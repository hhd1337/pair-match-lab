package com.locklab.pairmatch.common.exception;

import com.locklab.pairmatch.common.exception.dto.ErrorReasonDTO;
import com.locklab.pairmatch.common.exception.status.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private BaseErrorStatus status;

    // 에러 상태 및 메시지만 담은 DTO 반환
    public ErrorReasonDTO getErrorReason() {
        return this.status.getReason();
    }

    // HTTP 상태 코드까지 포함한 DTO 반환
    public ErrorReasonDTO getErrorReasonHttpStatus() {
        return this.status.getReasonHttpStatus();
    }
}
