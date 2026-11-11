# 코드 스타일

- Lombok 을 쓰지 않는다. 의존성은 `private final` 필드 + 생성자 주입.
- Command, Query, Request, Response, 값 객체는 `record`. 생성 시점 검증은 compact constructor 에서.
- `Optional` 은 반환 타입에만 쓴다. 필드·파라미터 금지.
- 컬렉션은 불변으로 반환한다 (`List.copyOf`).
- 매직 넘버·문자열은 상수 또는 enum 으로.
- `ThreadLocal` 대신 가능하면 `ScopedValue` (가상 스레드 환경, ADR-002).
- `synchronized` 대신 `ReentrantLock` 을 우선 고려한다. Java 25 에서 pinning 은 해소됐지만, 락 범위를 명시적으로 드러내기 위함이다.
- 주석은 "무엇"이 아니라 "왜"를 쓴다.
