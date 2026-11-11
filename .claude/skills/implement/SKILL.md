---
name: implement
description: TASKS.md 의 태스크 하나를 스펙에 따라 헥사고날 구조로 구현하고 유닛 테스트·커버리지까지 마칠 때 사용한다. 태스크 ID를 입력으로 받는다.
---

# implement

개발 사이클 3~5단계(구현 → 유닛 테스트 → 커버리지 검증)를 수행하고, 6단계(사람의 테스트 리뷰)에 넘길 자료를 만든다.

## 입력
- `TASKS.md` 의 태스크 ID (하나만)

## 먼저 읽을 것
- `TASKS.md` 의 해당 태스크
- 연결된 `docs/specs/*.md` 전체
- `docs/conventions/architecture.md`, `docs/conventions/code-style.md`

## 절차 (순서대로, 생략 금지)

1. **착수 표시**
   `TASKS.md` 에서 해당 태스크 상태를 `DOING` 으로 바꾼다. 다른 태스크가 `DOING` 이면 멈추고 보고한다.

2. **설계 요약 (코드 작성 전)**
   만들 클래스 목록을 레이어별로 적는다: in-port, Command/Query, service, out-port, adapter, 마이그레이션.
   스펙의 **7. 열린 질문**이 구현에 영향을 주면 여기서 멈추고 질문한다.

3. **안쪽부터 구현**
   `domain` → `application/port` → `application/service` → `adapter/in/web` → `adapter/out/persistence` 순서.
   테이블이 필요하면 `src/main/resources/db/migration/V{다음번호}__{설명}.sql` 을 추가한다.

4. **컴파일과 아키텍처 확인**
   `./gradlew test` 를 실행한다. `HexagonalArchitectureTest` 실패는 설계를 고쳐서 해결한다 (규칙을 고치지 않는다).

5. **유닛 테스트**
   `.claude/skills/unit-test/SKILL.md` 의 절차를 이 태스크의 측정 대상 클래스에 대해 그대로 수행한다.

6. **리뷰 자료 준비**
   `TASKS.md` 상태를 `REVIEW` 로 바꾸고 로그에 커버리지 수치와 발견 사항을 남긴다.

7. **보고** (아래 형식)

## 완료 기준
- `./gradlew test` 성공 (ArchUnit 포함)
- 측정 대상 클래스 `coverage.py` 종료 코드 0
- `TASKS.md` 상태 `REVIEW`

## 보고 형식
```
### implement 보고: {태스크 ID}
#### 만든 파일
| 레이어 | 파일 |
|---|---|

#### 스펙 대조표 (사람이 6단계에서 확인)
| 스펙 케이스 # | 기대 결과 | 테스트 DisplayName |
|---|---|---|
스펙 케이스 중 테스트가 없는 것, 테스트 중 스펙에 없는 것을 따로 표시한다.

#### 커버리지 (coverage.py 출력 그대로)

#### 발견 사항

#### 통합 테스트 필요 대상
adapter/out 등 integration-test 스킬로 넘길 클래스
```

## 금지
- 태스크 범위 밖 기능 추가
- 스펙 문서 수정 (질문으로 보고)
- 의존성 추가 (필요하면 이유와 함께 질문)
- `ddl-auto` 를 `validate` 이외로 변경
- 상태를 `DONE` 으로 바꾸기 (사람만 한다)
