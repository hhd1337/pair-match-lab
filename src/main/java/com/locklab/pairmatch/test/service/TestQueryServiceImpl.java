package com.locklab.pairmatch.test.service;

import com.locklab.pairmatch.common.exception.GeneralException;
import com.locklab.pairmatch.common.exception.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestQueryServiceImpl implements TestQueryService {
    @Override
    public void CheckFlag(Integer flag) {
        if (flag == 1) {
            throw new GeneralException(ErrorStatus.TEMP_EXCEPTION);
        }
    }
}
