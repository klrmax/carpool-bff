plugins {
    java
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.heroku"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}


repositories {
    mavenCentral()
}

dependencies {
    // --- Spring Boot Core ---
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("org.apache.commons:commons-dbcp2")
    implementation("org.json:json:20231013")

    // --- GraphQL ---
    implementation("org.springframework.boot:spring-boot-starter-graphql")

    // --- JWT Auth ---
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // --- Security (Password Hashing, Auth) ---
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-crypto")

    // --- Caching ---
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // --- Optional: Developer Tools ---
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // --- Tests ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.register("stage") {
    dependsOn("build")
}
