# 테스트 컨벤션

## 레이어별 테스트 방식

| 대상 | 방식 | 실행 태스크 | 커버리지 게이트 |
|---|---|---|---|
| `domain` | 순수 JUnit (Spring 없음) | `test` | LINE·BRANCH 100% |
| `application/service` | JUnit + Mockito, out-port 를 mock | `test` | LINE·BRANCH 100% |
| `adapter/in/web` | `@WebMvcTest(XxxController.class)` + `@MockitoBean` 으로 UseCase mock | `test` | LINE·BRANCH 100% |
| `adapter/out/persistence` | `@IntegrationTest` (Testcontainers 실제 DB) | `integrationTest` | 측정 안 함 |
| `adapter/in/websocket` | `@IntegrationTest` + STOMP 클라이언트 | `integrationTest` | 측정 안 함 |
| 아키텍처 규칙 | ArchUnit | `test` | - |

## 이름과 구조

```java
@Test
@DisplayName("[실패] 이미 가입된 이메일이면 DuplicateEmailException 이 발생한다")
void registerFailsWhenEmailAlreadyExists() {
    // given: 같은 이메일의 회원이 이미 존재한다
    given(loadMemberPort.existsByEmail("a@b.com")).willReturn(true);
    var command = new RegisterMemberCommand("a@b.com", "password1!", "nick");

    // when: 회원가입을 시도하면
    var thrown = catchThrowable(() -> service.register(command));

    // then: 중복 예외가 발생하고 저장은 일어나지 않는다
    assertThat(thrown).isInstanceOf(DuplicateEmailException.class);
    then(saveMemberPort).shouldHaveNoInteractions();
}
```

- `@DisplayName` 형식: `[성공|실패|경계] {조건}이면 {기대 결과}` — 한글, 스펙의 케이스 문장과 일치시킨다.
- 메서드 이름은 영어 camelCase 로 동작을 설명한다.
- `// given`, `// when`, `// then` 뒤에 **왜** 이 준비/행동/검증인지 한글로 쓴다.
- 테스트 하나는 하나의 동작만 검증한다.
- 검증은 AssertJ(`assertThat`), Mockito 는 BDD 스타일(`given`, `then`)을 쓴다.

## 금지
- 검증(`assert`, `then`)이 없는 테스트
- `@Disabled`, 빈 테스트로 커버리지만 올리기
- 리플렉션으로 private 메서드 호출
- 테스트 통과를 위해 기존 테스트의 기대값 변경 (필요하면 보고 후 결정 대기)
- 유닛 테스트에서 Spring 컨텍스트 띄우기 (`@SpringBootTest` 는 `@IntegrationTest` 로만)

## 테스트가 이상한 동작을 드러냈을 때
AI 는 현재 코드의 동작을 그대로 테스트로 옮기기 쉽다.
작성한 테스트가 **스펙과 다른 동작을 성공으로 기대**하게 된다면, 그 테스트는 작성하지 말고 "발견 사항"으로 보고한다.
예: 스펙상 페이지가 없는 디자인은 저장할 수 없는데, 코드는 저장을 허용한다 → 버그 후보로 보고.
