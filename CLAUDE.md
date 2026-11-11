# collab-canvas

실시간 협업 디자인 에디터의 백엔드. 문서·페이지·요소를 여러 사용자가 동시에 편집한다.

## 스택
- Java 25 (가상 스레드 사용, `spring.threads.virtual.enabled=true`), Spring Boot 4.1, Gradle Kotlin DSL
- PostgreSQL + JPA + QueryDSL(OpenFeign 포크) + Flyway: 회원, 문서 메타데이터, 멤버 권한
- MongoDB: 디자인 데이터(페이지·요소 트리), 스토리지 메타데이터
- Redis: 캐시, 토큰 저장, 문서별 접속자
- Lombok 사용하지 않음. DTO·Command·값 객체는 `record`

## 아키텍처: 헥사고날
루트 패키지 `io.github.parkgeounyong.collab_canvas` 아래 컨텍스트별로 나눈다.
```
{context}/domain/                 순수 자바. Spring·JPA 의존 금지
{context}/application/port/in/    유스케이스 인터페이스
{context}/application/port/out/   영속성·외부 시스템 인터페이스
{context}/application/service/    유스케이스 구현
{context}/adapter/in/web/         REST 컨트롤러
{context}/adapter/out/persistence JPA·Mongo·Redis 구현
global/                           설정, 공통 예외 처리
```
의존 방향은 바깥 → 안쪽만 허용한다. `HexagonalArchitectureTest`가 이를 강제한다.
상세: `docs/conventions/architecture.md`

## 명령어
| 목적 | 명령 |
|---|---|
| 유닛·슬라이스 테스트 + 커버리지 리포트 | `./gradlew test` |
| 커버리지 수치 확인 | `python3 .claude/scripts/coverage.py [파일명 일부]` |
| 통합 테스트 (Docker 필요) | `./gradlew integrationTest` |
| 전체 검증 (커버리지 100% 미달 시 실패) | `./gradlew build` |

## 테스트 규칙 (요약)
- 커버리지 기준: `domain`, `application/service`, `adapter/in/web` 의 LINE·BRANCH 100%
- `@DisplayName("[성공|실패|경계] 한글로 기대 동작")`, 본문은 `// given`, `// when`, `// then` + 한글 설명
- 통합 테스트는 클래스에 `@IntegrationTest` 를 붙인다
- 상세: `docs/conventions/testing.md`

## 반드시 지킬 것
- 커버리지·빌드 결과는 **명령을 실제로 실행한 출력**만 근거로 보고한다. 실행하지 않았으면 "실행하지 않음"이라고 쓴다.
- 테스트를 통과시키려고 기존 테스트의 기대값을 바꾸지 않는다. 바꿔야 한다면 이유를 보고하고 사람의 결정을 기다린다.
- 테스트가 스펙과 다른 동작을 드러내면 코드를 고치지 말고 **발견 사항으로 보고**한다.
- 한 번에 `TASKS.md` 의 태스크 하나만 처리한다.
- `git push` 하지 않는다.

## 작업 흐름
`docs/harness-guide.md` 의 8단계 개발 사이클을 따른다.
스펙: `docs/specs/`, 태스크: `TASKS.md`, 설계 결정: `docs/adr/`
