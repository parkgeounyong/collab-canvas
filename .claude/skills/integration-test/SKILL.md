---
name: integration-test
description: adapter/out/persistence, adapter/in/websocket 등 실제 인프라와 붙여야 검증되는 코드의 통합 테스트를 Testcontainers로 작성하거나, 동시성 시나리오를 검증할 때 사용한다.
---

# integration-test

개발 사이클 7단계를 수행한다.

## 전제
- Docker 가 실행 중이어야 한다. `docker info` 가 실패하면 테스트를 실행하지 말고 그 사실을 보고한다.
- 커버리지 게이트 대상이 아니다. 목표는 **실제 인프라에서의 동작 확인**이다.

## 절차 (순서대로, 생략 금지)

1. **대상과 시나리오 정리**
   어댑터가 구현한 port 메서드마다 검증할 시나리오를 적는다. 스펙에 동시성·순서 관련 정책이 있으면 시나리오로 포함한다.

2. **테스트 작성**
   - 클래스에 `@IntegrationTest` 를 붙인다 (`support/IntegrationTest`). 컨테이너를 새로 정의하지 않는다.
   - mock 을 쓰지 않는다. port 인터페이스를 주입받아 실제 어댑터를 호출한다.
   - 테스트 간 데이터가 섞이지 않게 `@AfterEach` 에서 정리하거나 테스트마다 고유 식별자를 쓴다.
   - DisplayName·Given/When/Then 규칙은 유닛 테스트와 같다.

3. **동시성 시나리오 (5단계 이후)**
   - 가상 스레드 executor(`Executors.newVirtualThreadPerTaskExecutor()`)로 N 개 작업을 만들고, `CountDownLatch` 로 동시에 출발시킨다.
   - 검증은 "최종 상태가 기대와 같다" 형태로 쓴다. 실행 순서에 의존하는 단언 금지.
   - 비결정적 문제를 잡기 위해 `@RepeatedTest` 로 여러 번 돌린다.

4. **실행**
   `./gradlew integrationTest --tests '{클래스 FQCN}'` 를 실행한다. 실패 원인을 테스트 오류 / 어댑터 버그 / 인프라 문제로 분류한다.

5. **보고** (아래 형식)

## 보고 형식
```
### integration-test 보고: {태스크 ID}
- 실행 명령과 결과: {성공/실패, 테스트 수}
| DisplayName | 검증한 port 메서드 / 시나리오 |
|---|---|
#### 발견 사항
```

## 금지
- Docker 없이 실행한 척 보고하기
- `@SpringBootTest` 를 직접 붙이기 (`@IntegrationTest` 사용)
- 통합 테스트로 도메인 로직 분기 검증하기 (유닛 테스트 몫)
