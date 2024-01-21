import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("com.netflix.dgs.codegen") version "6.0.3"
	id("org.springframework.boot") version "3.1.5"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

group = "com.zach-jones"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:latest.release")
	}
}
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.json:json:20231013")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")
	testImplementation("io.kotest:kotest-assertions-core:5.8.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.bootBuildImage {
	builder.set("paketobuildpacks/builder-jammy-base:latest")
}

tasks.withType<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask> {
	generateClient = false
	packageName = "com.zachjones.languageclassifier.model"
	schemaPaths = mutableListOf("${projectDir}/src/main/resources/schema")
}
