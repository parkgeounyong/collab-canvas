# 아키텍처 컨벤션

## 패키지 구조
```
io.github.parkgeounyong.collab_canvas
├── member/ auth/ document/ collaboration/ presence/   ← 컨텍스트
│   ├── domain/
│   ├── application/
│   │   ├── port/in/
│   │   ├── port/out/
│   │   └── service/
│   └── adapter/
│       ├── in/web/
│       ├── in/websocket/        (4단계부터)
│       └── out/persistence/
└── global/
    ├── config/
    └── error/
```

## 레이어별 책임과 규칙

| 레이어 | 책임 | 허용 의존 | 금지 |
|---|---|---|---|
| `domain` | 비즈니스 규칙, 상태 변경, 불변식 검증 | JDK | Spring, JPA, application, adapter |
| `application/port/in` | 유스케이스 계약 | domain | 구현 세부 |
| `application/port/out` | 바깥 세계에 대한 요구 | domain | JPA 타입 노출 |
| `application/service` | 유스케이스 흐름 조율, 트랜잭션 경계 | domain, port, Spring(`@Service`, `@Transactional`) | adapter, JPA |
| `adapter/in/web` | HTTP ↔ Command 변환, 응답 매핑 | port/in | port/out, adapter/out |
| `adapter/out/persistence` | port/out 구현, 엔티티 매핑, 쿼리 | port/out, domain, JPA, QueryDSL | adapter/in |

규칙은 `src/test/.../architecture/HexagonalArchitectureTest.java` 가 자동 검증한다.

## 명명 규칙

| 종류 | 형식 | 예시 |
|---|---|---|
| 인바운드 포트 | `{동사}{대상}UseCase` | `RegisterMemberUseCase` |
| 유스케이스 입력 | `{동사}{대상}Command` / `{대상}Query` (record) | `RegisterMemberCommand` |
| 서비스 | `{동사}{대상}Service` | `RegisterMemberService` |
| 아웃바운드 포트 | `Load{대상}Port`, `Save{대상}Port` | `LoadMemberPort` |
| 영속성 어댑터 | `{대상}PersistenceAdapter` | `MemberPersistenceAdapter` |
| JPA 엔티티 | `{대상}JpaEntity` | `MemberJpaEntity` |
| Mongo 문서 | `{대상}Document` | `DesignPageDocument` |
| 컨트롤러 | `{대상}Controller` | `MemberController` |
| 요청·응답 | `{동작}Request`, `{대상}Response` (record) | `SignUpRequest` |

## 도메인 모델과 영속성 모델 분리
- 도메인 객체(`Member`)와 JPA 엔티티(`MemberJpaEntity`)는 **별도 클래스**다.
- 변환은 `adapter/out/persistence` 안의 매퍼가 담당한다. 도메인은 매핑을 모른다.
- QueryDSL(`JPAQueryFactory`, Q클래스)은 `adapter/out/persistence` 밖으로 나가지 않는다.

이유: 도메인을 프레임워크 없이 테스트할 수 있어야 유닛 테스트가 빠르고, 커버리지 100%를 유지하는 비용이 낮아진다. (ADR-003)

## 예외
- 도메인 규칙 위반: `domain` 안의 예외(예: `DuplicateEmailException`)를 던진다.
- HTTP 상태 코드 변환은 `global/error` 의 `@RestControllerAdvice` 한 곳에서만 한다.
