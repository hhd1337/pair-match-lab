package com.locklab.pairmatch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NamedLockRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * MySQL 네임드 락 획득 시도
     *
     * @param lockName   락 이름 (전역 key)
     * @param timeoutSec 최대 대기 시간(초)
     * @return true: 락 획득 성공, false: 타임아웃 등으로 실패
     */
    public boolean acquireLock(String lockName, long timeoutSec) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT GET_LOCK(?, ?)",
                Integer.class,
                lockName,
                timeoutSec
        );

        return result != null && result == 1; // GET_LOCK 결과: 1(성공), 0(타임아웃), NULL(에러)
    }

    /**
     * MySQL 네임드 락 해제
     *
     * @param lockName 락 이름
     */
    public void releaseLock(String lockName) {
        jdbcTemplate.queryForObject(
                "SELECT RELEASE_LOCK(?)",
                Integer.class,
                lockName
        );
    }
}
