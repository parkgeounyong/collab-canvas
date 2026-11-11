plugins {
	java
	jacoco
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.parkgeounyong"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

val querydslVersion = "7.7"      // OpenFeign 포크. 원조(com.querydsl)는 Hibernate 7 미지원
val archunitVersion = "1.4.1"

dependencies {
	// Web
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// PostgreSQL + JPA + Flyway + QueryDSL
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	implementation("io.github.openfeign.querydsl:querydsl-jpa:$querydslVersion")
	annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:$querydslVersion:jpa")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")

	// MongoDB, Redis, Cache
	implementation("org.springframework.boot:spring-boot-starter-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-cache")

	// 로컬 실행 시 compose.yaml 자동 기동 (배포 jar에는 포함되지 않음)
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-cache-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test")
	testImplementation("org.springframework.boot:spring-boot-starter-mongodb-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.testcontainers:testcontainers-mongodb")
	testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ─────────────────────────────────────────────────────────────
// 테스트 분리
//   test            : 유닛·슬라이스 테스트 (빠름, 커버리지 측정 대상)
//   integrationTest : @Tag("integration") 테스트 (Testcontainers, 느림)
// ─────────────────────────────────────────────────────────────
tasks.test {
	description = "Runs unit and slice tests (excludes @Tag(\"integration\"))."
	useJUnitPlatform {
		excludeTags("integration")
	}
	finalizedBy(tasks.jacocoTestReport)
}

val integrationTest by tasks.registering(Test::class) {
	description = "Runs integration tests tagged with @Tag(\"integration\")."
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	testClassesDirs = sourceSets.test.get().output.classesDirs
	classpath = sourceSets.test.get().runtimeClasspath
	useJUnitPlatform {
		includeTags("integration")
	}
	shouldRunAfter(tasks.test)
}

// ─────────────────────────────────────────────────────────────
// 커버리지
//   측정 범위: domain, application.service, adapter.in.web
//   기준    : 클래스 단위 LINE·BRANCH 100%
// ─────────────────────────────────────────────────────────────
val coverageIncludes = listOf(
	"**/domain/**",
	"**/application/service/**",
	"**/adapter/in/web/**",
)
val coverageExcludes = listOf(
	"**/global/**",
	"**/*Application*",
)
val coverageClasses = sourceSets.main.get().output.asFileTree.matching {
	include(coverageIncludes)
	exclude(coverageExcludes)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	classDirectories.setFrom(coverageClasses)
	reports {
		html.required = true   // 사람용
		xml.required = true    // 스크립트·AI용 (라인 번호 포함)
		csv.required = false
	}
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.test)
	classDirectories.setFrom(coverageClasses)
	violationRules {
		rule {
			element = "CLASS"
			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "1.0".toBigDecimal()
			}
			limit {
				counter = "BRANCH"
				value = "COVEREDRATIO"
				minimum = "1.0".toBigDecimal()
			}
		}
	}
}

// ./gradlew check (= build) 가 커버리지 미달이면 실패하도록 연결
tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification, integrationTest)
}
