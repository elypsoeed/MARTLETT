plugins {
	java
	checkstyle
	id("org.openapi.generator") version "7.22.0"
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

import org.springframework.boot.gradle.tasks.run.BootRun
import java.util.Properties

group = "com.elypsoeed"
version = "0.0.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.1")
	}
}

springBoot {
	mainClass.set("com.elypsoeed.martlett.MartlettApplication")
}

dependencies {
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("org.springframework.cloud:spring-cloud-starter-vault-config")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
	implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.31")
	implementation("io.swagger.core.v3:swagger-models-jakarta:2.2.31")
	compileOnly("org.jspecify:jspecify:1.0.0")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("io.rest-assured:rest-assured:5.5.6")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
	testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
	testCompileOnly("org.jspecify:jspecify:1.0.0")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val vaultBootstrapScript = layout.projectDirectory.file("ops/vault/bootstrap-dev.sh")
val vaultAppEnvFile = layout.projectDirectory.file("ops/vault/.local/martlett-app.env")

val bootstrapVaultDev by tasks.registering(Exec::class) {
	group = "application"
	description = "Bootstraps local Vault and prepares AppRole credentials for bootRun."
	commandLine(vaultBootstrapScript.asFile.absolutePath)
}

tasks.named<BootRun>("bootRun") {
	dependsOn(bootstrapVaultDev)

	doFirst {
		val envFile = vaultAppEnvFile.asFile
		if (!envFile.isFile) {
			throw GradleException("Vault app env file not found: ${envFile.absolutePath}")
		}

		val properties = Properties()
		envFile.inputStream().use(properties::load)

		listOf("VAULT_APPROLE_ROLE_ID", "VAULT_APPROLE_SECRET_ID").forEach { key ->
			val value = properties.getProperty(key)
			if (value.isNullOrBlank()) {
				throw GradleException("Missing required Vault bootstrap value: $key")
			}
			environment(key, value)
		}
	}
}

checkstyle {
	toolVersion = "10.26.1"
	configFile = file("config/checkstyle/checkstyle.xml")
}

tasks.withType<Checkstyle>().configureEach {
	reports {
		xml.required = false
		html.required = true
	}
}

tasks.named<Checkstyle>("checkstyleMain") {
	source = fileTree("src/main/java")
}

tasks.named<Checkstyle>("checkstyleTest") {
	source = fileTree("src/test/java")
}

tasks.register("checkstyleAll") {
	group = "verification"
	description = "Runs Checkstyle for main and test sources."
	dependsOn(tasks.named("checkstyleMain"), tasks.named("checkstyleTest"))
}

val openApiOutputDir = layout.buildDirectory.dir("generated/openapi")

sourceSets {
	named("main") {
		java.srcDir(openApiOutputDir.map { it.dir("src/main/java") })
	}
}

openApiGenerate {
	generatorName.set("spring")
	inputSpec.set("$projectDir/src/main/resources/openapi/openapi-config.yaml")
	outputDir.set(openApiOutputDir.get().asFile.absolutePath)
	apiPackage.set("com.elypsoeed.martlett.generated.api")
	modelPackage.set("com.elypsoeed.martlett.generated.model")
	invokerPackage.set("com.elypsoeed.martlett.generated.invoker")
    skipValidateSpec.set(true)
    configOptions.set(
        mapOf(
            "openApiNullable" to "false",
            "useSwaggerUI" to "false",
            "interfaceOnly" to "true",
            "useTags" to "true",
            "implicitHeaders" to "true",
            "library" to "spring-boot",
            "useSpringBoot4" to "true",
            "useJakartaEe" to "true",
            "useBeanValidation" to "true",
            "useJakartaValidation" to "true",
            "lombok" to "true",
        )
    )
}

tasks.named("compileJava") {
	dependsOn(tasks.named("openApiGenerate"))
}
