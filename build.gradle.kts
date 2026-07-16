plugins {
    java
    id("org.springframework.boot") version "3.5.15"
    id("io.spring.dependency-management") version "1.1.7"
    // id("org.asciidoctor.jvm.convert") version "4.0.3"
}

description = "Shopping Cart Management Service for Bakery"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
	mavenLocal()
	mavenCentral()
	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/amankrmj09/bakery-common-libs")
		credentials {
			username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
			password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
		}
	}
}

// extra["snippetsDir"] = file("build/generated-snippets")
extra["springCloudVersion"] = "2025.0.3"

dependencies {
	// 1. Shared Custom Libraries
	implementation("org.blubugtech.com:common-libs:2.0.0")

	// 2. Spring Boot Core & Web
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// 3. Spring Cloud & Discovery
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	implementation("org.springframework.cloud:spring-cloud-starter-config")

	// 4. Data & Persistence
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")

	// 6. Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// 7. Third-Party Utilities (Jackson, AWS, etc.)
	implementation("com.fasterxml.jackson.core:jackson-core")
	implementation("com.fasterxml.jackson.core:jackson-annotations")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	// 8. Tooling & Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	// runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	// 9. Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// testImplementation("org.springframework.boot:spring-boot-testcontainers")
	// testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	// testImplementation("org.testcontainers:junit-jupiter")
	// testImplementation("org.testcontainers:postgresql")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// tasks.test {
//     outputs.dir(project.extra["snippetsDir"]!!)
// }
//
// tasks.asciidoctor {
//     inputs.dir(project.extra["snippetsDir"]!!)
//     dependsOn(tasks.test)
// }

