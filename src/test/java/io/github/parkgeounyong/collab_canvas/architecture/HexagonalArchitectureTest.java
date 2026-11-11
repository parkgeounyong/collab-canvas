package io.github.parkgeounyong.collab_canvas.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 헥사고날 의존 방향 규칙. 위반 시 ./gradlew test 가 실패한다.
 * 규칙을 바꾸려면 docs/conventions/architecture.md 와 함께 ADR 을 남긴다.
 */
@AnalyzeClasses(
	packages = "io.github.parkgeounyong.collab_canvas",
	importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureTest {

	@ArchTest
	static final ArchRule 도메인은_프레임워크와_바깥_계층을_모른다 = noClasses()
		.that().resideInAPackage("..domain..")
		.should().dependOnClassesThat().resideInAnyPackage(
			"org.springframework..",
			"jakarta.persistence..",
			"..application..",
			"..adapter.."
		)
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule 애플리케이션은_어댑터와_JPA를_모른다 = noClasses()
		.that().resideInAPackage("..application..")
		.should().dependOnClassesThat().resideInAnyPackage(
			"..adapter..",
			"jakarta.persistence.."
		)
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule 인바운드_어댑터는_아웃바운드_어댑터를_모른다 = noClasses()
		.that().resideInAPackage("..adapter.in..")
		.should().dependOnClassesThat().resideInAPackage("..adapter.out..")
		.allowEmptyShould(true);

}
