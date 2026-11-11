# ADR-002: Java 25 와 가상 스레드

- 상태: 채택
- 날짜: 2026-11-10

## 결정
Java 25(LTS) + Spring Boot 4.1, `spring.threads.virtual.enabled=true`.

## 이유
- 가상 스레드는 21에서 정식이 됐지만, `synchronized` 안의 블로킹 I/O 가 캐리어 스레드를 고정(pinning)하는 문제가 있었다. 24에서 대부분 해소됐고, 25는 이를 포함한 첫 LTS다.
- 25에서 정식화된 Scoped Values 를 요청 컨텍스트 전파에 쓸 수 있다.

## 주의
- 스레드는 싸지만 DB 커넥션은 비싸다. Hikari 풀 크기가 실제 동시성 상한이다.
- Structured Concurrency 는 25에서도 preview 라 쓰지 않는다.
