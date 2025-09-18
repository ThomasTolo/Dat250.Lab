plugins {
	java
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.7"
	
}

group = "no.hvl"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.6.0")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework:spring-messaging")
	implementation("org.hibernate.orm:hibernate-core:6.4.4.Final")
    implementation("com.h2database:h2:2.2.224")
	implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    

}

tasks.withType<Test> {
	useJUnitPlatform()
}







