package io.github.parkgeounyong.collab_canvas.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * 통합 테스트 표식. 이 어노테이션이 붙은 테스트는
 * ./gradlew test 에서 제외되고 ./gradlew integrationTest 에서만 실행된다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("integration")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public @interface IntegrationTest {
}
