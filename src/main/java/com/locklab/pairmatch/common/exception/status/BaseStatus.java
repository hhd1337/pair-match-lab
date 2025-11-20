package com.locklab.pairmatch.common.exception.status;

import com.locklab.pairmatch.common.exception.dto.ReasonDTO;

public interface BaseStatus {
    ReasonDTO getReason();

    ReasonDTO getReasonHttpStatus();
}
