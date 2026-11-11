# ADR-003: 헥사고날 구조와 영속성 모델 분리

- 상태: 채택
- 날짜: 2026-11-10

## 결정
- 컨텍스트별 `domain / application(port, service) / adapter(in, out)` 구조.
- 도메인 모델과 JPA 엔티티를 분리하고, 매핑은 `adapter/out/persistence` 가 맡는다.
- QueryDSL 은 OpenFeign 포크(7.x)를 쓰고, 영속성 어댑터 밖으로 노출하지 않는다.
- 의존 방향은 ArchUnit(`HexagonalArchitectureTest`)으로 강제한다.

## 이유
도메인을 프레임워크 없이 테스트할 수 있어야 유닛 테스트가 빠르고 커버리지 100% 유지 비용이 낮다.

## 비용
매퍼 코드가 늘어난다. 데모 규모에서는 감수한다.
