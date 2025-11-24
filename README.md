# 우테코 프리코스 4·5주차 오픈미션 - 공정한 페어 매칭을 보장하는 락 전략 비교 실험

이번 오픈미션의 목표는 “공정한 페어 매칭을 위한 동시성 제어 전략을 직접 설계하고, 여러 락 전략을 비교, 검증하는 것”이다.  
단순히 동작하는 매칭 시스템을 만드는 것이 아니라,  
**다중 서버 환경에서도 중복 매칭이 일어나지 않는 공정한 매칭 로직을 보장하는 것**을 핵심 문제로 설정했다.

이를 위해 다양한 락 전략을 직접 구현하고 실험하며,  
각 전략의 **장점, 단점, 트레이드오프**를 비교하는 분산락 실험 플랫폼을 구축했다.

<오픈미션 과정 전체 기록>    
https://peach-sodalite-6db.notion.site/2a6f043da404807d8215dcbca6fb3066?pvs=74

---

## 1. 프로젝트 개요

### 📌 문제 정의  
다중 서버 환경(단일DB사용)에서 매칭 요청이 각 서버로 동시에 들어올 때, 하나의 크루가 두 번 매칭되거나(pair_history 중복), 이미 매칭된 크루가 다시 다른 요청에서 선점되는 문제가 발생할 수 있다.

즉, 매칭 로직이 임계영역(Critical Section)을 포함하고 있으며, 이를 보호하지 않으면 Race Condition이 반드시 발생한다.

### 📌 해결 전략  
락 전략을 플러그인 형태로 교체 가능한 구조(LockingStrategy)를 만들고, 아래 다섯 전략을 직접 구현하여 실험했다.

- **NONE** (락 없음)
- **LOCAL (synchronized)**
- **DB_PESSIMISTIC** (SELECT ... FOR UPDATE)
- **MYSQL_NAMED_LOCK** (GET_LOCK / RELEASE_LOCK)
- **REDIS_LOCK** (SETNX + TTL + Owner 기반 분산락)

모든 전략은 동일한 `PairMatchService`를 기준으로 교체되며, 단일 서버, 멀티 서버 환경에서 서로 다른 결과를 만들어낸다. 로그를 통해 


---

## 2. 구현 기능 목록

### 2.1 페어 매칭 기능

- `Crew` 목록을 조회하고 `matched=false`인 크루만 대상으로 매칭한다.
- 목록을 랜덤으로 섞은 뒤, 앞에서부터 순서대로 두 명씩 페어를 구성한다.
- 남은 인원이 3명일 경우 3인 페어를 구성한다.
- 같은 레벨에서 이미 매칭된 적 있는 조합(PairHistory)이 있으면 매칭을 재시도한다.
- 모든 매칭은 반드시 특정 락 전략 아래에서 보호된다.
- 임계영역:
  - `crew.matched` 상태 변경
  - `pair_history` 생성
  - `pair_group` / `pair_member` 생성


### 2.2 락 전략 선택 및 비교 기능

- `/match?lockType=XXXX` 형태로 요청 시 서버가 해당 LockingStrategy 구현체를 선택한다.
- 전략 패턴 기반 LockingStrategyRouter로 분기 처리.
- 단일 서버: 멀티스레드 경쟁으로 Race Condition 재현.
- 다중 서버(8080, 8081): 전략별로 다른 동작 분석.

### 2.3 통계/로그 기반 실험 분석

- 전략별 락 획득/대기/해제 로그 기록
- 중복 매칭 여부
- Redis 스핀 + 백오프 동작 확인

CSV 그래프 시각화는 선택사항이었으나, 로그와 실험 기반 분석을 중심으로 진행했다.


---

## 3. 락 전략 비교 요약

| 전략 | 단일 서버 | 다중 서버 | 중복 매칭 | 특징 |
|------|-----------|------------|------------|--------|
| NONE | ❌ | ❌ | 발생 | Race Condition 그대로 노출 |
| LOCAL (synchronized) | ⭕ | ❌ | 발생 | JVM 내부 락. 서버 확장 시 무력화 |
| DB_PESSIMISTIC | ⭕ | ⭕ | 없음 | 안정적이나 DB 커넥션 대기 증가 |
| MYSQL_NAMED_LOCK | ⭕ | ⭕ | 없음 | DB 커넥션 기반 전역 락. 느림 |
| REDIS_LOCK | ⭕ | ⭕ | 없음 | 빠르고 확장성이 가장 높음 |


---

## 4. 로컬 개발 환경

### MySQL
- DB: `pairmatchdb`
- USER: `root`
- PASSWORD: `1234`  
(⚠ 개발용이며 운영 환경에서는 절대 사용 금지)

### Redis
- 로컬 Redis 또는 Docker Redis 사용 가능  
- 락 키 형식: `lock:pair-match:mission:{id}`


---

## 5. 실행 방법

아래 과정은 동시성 실험을 재현하기 위한 전체 실행 흐름이다.

DB 초기 세팅 → Redis → 서버 두 개 띄우기 → PowerShell 동시 요청 스크립트까지 모두 포함한다.

---

### 5.1 MySQL 초기 설정

1. DB 생성

```sql
CREATE DATABASE pairmatchdb;

```

2. `mission` 테이블에 테스트용 레코드 생성

예시:

```sql
USE pairmatchdb;

INSERT INTO mission (id, name, content, level, mission_order_in_level)
VALUES
    (1, '문자열 덧셈 계산기', '문자열 덧셈 계산기 제작해봅시다', 1, 1),
    (2, '자동차 경주', '자동차 경주 자바 코드로 짜시오..!', 1, 2),
    (3, '로또 발생기', '로또 발생기 제작해봅시다.', 1, 3);

```
<img width="2586" height="216" alt="image" src="https://github.com/user-attachments/assets/1599fe72-8c64-4d6d-8607-61f2babbabe9" />

- `id`는 원하는 실험 missionId를 직접 지정하면 된다.
- 여러 실험을 하려면 mission을 여러 개 만들어도 된다.

3. `crew` 테이블에 테스트 크루 데이터 생성

```sql
USE pairmatchdb;

INSERT INTO crew (name, matched)
VALUES
    ('크루01', FALSE),
    ('크루02', FALSE),
    ('크루03', FALSE),
    ('크루04', FALSE),
    ('크루05', FALSE),
    ('크루06', FALSE),
    ('크루07', FALSE),
    ('크루08', FALSE),
    ('크루09', FALSE),
    ('크루10', FALSE),
    ('크루11', FALSE);

```
<img width="1670" height="529" alt="image" src="https://github.com/user-attachments/assets/16e52632-5be7-417d-a9bb-474cf2214e36" />

- 최소 2명 이상이어야 매칭 가능.
- 11명 이상을 넣어두면 여러 테스트가 수월하다.

---

### 5.2 Redis 실행

Docker를 사용하는 경우:

```bash
docker run --name redis-locklab -p 6379:6379 -d redis:7-alpine

```

로컬 설치 Redis도 문제없이 사용 가능하다.

---

### 5.3 Spring 애플리케이션 2개 포트에서 실행

멀티 서버 환경 실험을 위해 동일한 애플리케이션을 두 개 실행한다.

IntelliJ 기준

1. Run/Debug Configurations → Add New Configuration → Spring Boot
2. 첫 번째 서버: 기본 포트 8080 실행
3. 두 번째 서버:
    - `VM options`에 다음 추가
        
        ```
        -Dserver.port=8081
        ```
        
    - 이후 8081 포트로 애플리케이션 실행

---

### 5.4 동시 요청 스크립트 실행 (PowerShell)

20×2 = 총 40개의 요청이 8080, 8081 서버로 동시에 전송된다.

```bash
$jobs = @()

for ($i = 1; $i -le 20; $i++) {
    $jobs += Start-Job -ScriptBlock {
        Invoke-WebRequest -Uri "http://localhost:8080/match/1?lock=REDIS" -Method POST | Out-Null
    }
    $jobs += Start-Job -ScriptBlock {
        Invoke-WebRequest -Uri "http://localhost:8081/match/1?lock=REDIS" -Method POST | Out-Null
    }
}

Wait-Job $jobs
Receive-Job $jobs | Out-Null

```
`http://localhost:8081/match/1?lock=REDIS` 에서 1은 missionId이고, REDIS는 락 타입 enum이다.  
따라서 아래와 같이 진행한다.
1. 아까 만든 미션 레코드 중 매칭을 원하는 미션의 id를 자유롭게입력한다. 
2. 락타입은 NONE, JAVA_LOCAL, DB_PESSIMISTIC, MYSQL_NAMED, REDIS 가 있다. 원하는 락 타입을 선택하여 자유롭게 동시 요청 스크립트를 변형하여 요청을 보낸다.

즉, 파워쉘 동시 요청 스크립트에서 요청 url 부분을 다음과 같이 바꾸면 각 전략별 실험을 수행할 수 있다:

```bash
"http://localhost:8080/match/1?lock=NONE"
"http://localhost:8080/match/1?lock=JAVA_LOCAL"
"http://localhost:8080/match/2?lock=DB_PESSIMISTIC"
"http://localhost:8080/match/3?lock=MYSQL_NAMED"
"http://localhost:8080/match/3?lock=REDIS"

```

---

### 5.5 실험 결과 확인

PowerShell에서 확인

- 응답 속도
- 요청 처리 성공/실패 여부

IntelliJ에서 확인 (각 서버별 로그)

- 락 획득 로그
- 락 대기 발생 여부
- 중복 매칭 발생 여부
- 전략별 처리 속도 차이
- Redis 스핀락 + 백오프 동작 여부
