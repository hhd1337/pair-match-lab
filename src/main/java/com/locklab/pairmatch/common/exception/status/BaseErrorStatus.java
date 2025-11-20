package com.locklab.pairmatch.common.exception.status;

import com.locklab.pairmatch.common.exception.dto.ErrorReasonDTO;

public interface BaseErrorStatus {
    ErrorReasonDTO getReason();

    ErrorReasonDTO getReasonHttpStatus();
}
