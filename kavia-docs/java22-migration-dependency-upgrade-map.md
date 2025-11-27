# Java 8 to Java 22 Migration: Dependency Upgrade Map

## Overview

This document inventories the current dependencies and plugins used by the project and proposes compatible targets for a successful migration to Java 22. It highlights breaking changes, especially those related to the Jakarta EE namespace transition and Spring ecosystem upgrades.

The project is currently:
- Built with Java 8
- Based on Spring Boot 2.1.8.RELEASE (parent POM)
- Using Spring Security 5.x and Thymeleaf
- Using JPA (Hibernate) with javax.persistence
- Using H2 (dev) and MySQL (prod) with Flyway migrations

Target state:
- Build and run on Java 22
- Spring Boot 3.3.x (supports Java 22)
- Jakarta EE 9+ namespaces (jakarta.*)
- JUnit 5 (Jupiter) for tests
- Updated Maven plugins (compiler, surefire, failsafe)
- Updated database drivers and tools compatible with Java 22

Notes on “current version”: where the version is not explicitly declared in the pom.xml, it is managed by the Spring Boot 2.1.8.RELEASE BOM. Use mvn help:effective-pom or mvn dependency:tree -Dverbose to view the exact resolved versions in your environment.

## Current Dependency and Plugin Inventory (from code)

Parent:
- org.springframework.boot:spring-boot-starter-parent = 2.1.8.RELEASE

Properties:
- java.version = 1.8

Dependencies:
- org.springframework.boot:spring-boot-starter-thymeleaf (version managed by Spring Boot 2.1.8.RELEASE)
- org.thymeleaf.extras:thymeleaf-extras-springsecurity5 (managed by Spring Boot 2.1.8.RELEASE)
- org.springframework.boot:spring-boot-starter-web (managed by Spring Boot 2.1.8.RELEASE)
- org.springframework.boot:spring-boot-devtools (runtime, managed by Spring Boot 2.1.8.RELEASE)
- org.projectlombok:lombok (optional; effective Lombok version used by annotationProcessorPaths is 1.18.28)
- org.springframework.boot:spring-boot-starter-data-jpa (managed by Spring Boot 2.1.8.RELEASE)
- com.h2database:h2 (runtime; managed by Spring Boot 2.1.8.RELEASE)
- mysql:mysql-connector-java (managed by Spring Boot 2.1.8.RELEASE)
- org.flywaydb:flyway-core (managed by Spring Boot 2.1.8.RELEASE)
- org.springframework.boot:spring-boot-starter-security (managed by Spring Boot 2.1.8.RELEASE)
- org.springframework.boot:spring-boot-starter-test (test; managed by Spring Boot 2.1.8.RELEASE; currently using JUnit 4 in code)

Plugins:
- maven-compiler-plugin = 3.8.1
  - configuration: <source>1.8</source>, <target>1.8</target>, <release>8</release>, annotationProcessorPaths Lombok 1.18.28
- maven-surefire-plugin = 2.22.2
- org.springframework.boot:spring-boot-maven-plugin = 2.1.8.RELEASE

## Proposed Target Versions (Java 22 compatible)

Parent:
- org.springframework.boot:spring-boot-starter-parent = 3.3.x (e.g., 3.3.5)

Properties:
- java.version = 22 (or maven.compiler.release = 22)

Dependencies:
- org.springframework.boot:spring-boot-starter-thymeleaf (managed by Spring Boot 3.3.x)
- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 (replaces springsecurity5)
- org.springframework.boot:spring-boot-starter-web (managed by Spring Boot 3.3.x)
- org.springframework.boot:spring-boot-devtools (runtime, managed by Spring Boot 3.3.x)
- org.projectlombok:lombok (set to ≥ 1.18.34; keep as optional; no explicit plugin annotationProcessorPaths needed)
- org.springframework.boot:spring-boot-starter-data-jpa (managed by Spring Boot 3.3.x; Hibernate 6.x)
- com.h2database:h2 (runtime; managed by Spring Boot 3.3.x; H2 2.x)
- com.mysql:mysql-connector-j (replaces mysql:mysql-connector-java; version managed by Boot 3.3.x; e.g., 8.4 LTS line)
- org.flywaydb:flyway-core (managed by Spring Boot 3.3.x; Flyway 10.x)
- org.springframework.boot:spring-boot-starter-security (managed by Spring Boot 3.3.x; Spring Security 6.x)
- org.springframework.boot:spring-boot-starter-test (managed by Spring Boot 3.3.x; JUnit 5 Jupiter)

Plugins:
- maven-compiler-plugin = 3.11.x (configure <release>22</release>)
- maven-surefire-plugin = 3.2.5 (JUnit 5 support)
- maven-failsafe-plugin = 3.2.5 (optional; for integration tests)
- org.springframework.boot:spring-boot-maven-plugin = 3.3.x

## Breaking Changes and Migration Notes

### 1) Jakarta EE Namespace Migration (critical)
- Replace javax.persistence.* with jakarta.persistence.* in all entities, repositories, and any JPA usage.
- If javax.validation.* is used, replace with jakarta.validation.*.
- No javax.servlet.* imports are present in this codebase; if added later, those must become jakarta.servlet.*.

Expected changes in files:
- src/main/java/com/ezlearning/platform/auth/User.java
- src/main/java/com/ezlearning/platform/model/Curso.java
- src/main/java/com/ezlearning/platform/model/Profesor.java
- src/main/java/com/ezlearning/platform/model/Matricula.java
- Any other classes that import javax.persistence.*

### 2) Spring Security 6
- WebSecurityConfigurerAdapter is removed. Define a SecurityFilterChain @Bean instead.
- antMatchers(...) is replaced by requestMatchers(...) in authorizeHttpRequests DSL.
- @EnableGlobalMethodSecurity becomes @EnableMethodSecurity.
- Logout and CSRF configuration moves to the lambda-based DSL.

Expected change:
- src/main/java/com/ezlearning/platform/security/ApplicationSecurityConfiguration.java → new style configuration class.

### 3) Thymeleaf Security Extras
- org.thymeleaf.extras:thymeleaf-extras-springsecurity5 → thymeleaf-extras-springsecurity6
- Thymeleaf templates using the sec: dialect continue to work; the dialect artifact aligns with Spring Security 6.

### 4) Hibernate/JPA
- Hibernate 6.x under Boot 3 has improvements and some differences:
  - Ensure imports use jakarta.persistence.
  - Query and naming behaviors are mostly compatible; verify any native query or HQL usage.
  - Lazy loading semantics remain, but test for runtime behavior.

### 5) H2 (Dev profile)
- Upgrades from 1.4.x to 2.x can require adjustments:
  - Driver remains org.h2.Driver.
  - If SQL compatibility issues arise, consider adding ;MODE=MySQL to the URL during development; not typically necessary with simple schema.
- The project’s SQL schema and data scripts should remain compatible; test the H2 console and migrations.

### 6) MySQL Driver Coordinates
- Switch from mysql:mysql-connector-java to com.mysql:mysql-connector-j.
- spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver remains valid (already present in application-prod.properties).

### 7) Flyway 10.x
- Migrations under src/main/resources/db/migration remain correctly named V1.1__schema.sql and V1.2__data.sql.
- If Flyway fails due to baseline issues in existing environments, set flyway.baselineOnMigrate=true for first upgrade deployment (do not commit long-term if not needed).

### 8) Testing Stack (JUnit 5)
- Convert tests from JUnit 4 to JUnit 5:
  - Remove @RunWith(SpringRunner.class)
  - Use org.junit.jupiter.api.Test
  - surefire 3.2.5 runs Jupiter tests by default when using spring-boot-starter-test on Boot 3.x.

## Summary Matrix

- Spring Boot: 2.1.8.RELEASE → 3.3.x (Java 22 support; Jakarta; Spring Framework 6)
- Spring Security: 5.x → 6.x (no WebSecurityConfigurerAdapter; new DSL)
- Thymeleaf Extras: springsecurity5 → springsecurity6
- JPA/Hibernate: javax.* → jakarta.*; Hibernate 6.x
- H2: 1.4.x → 2.x (managed by Boot 3.3.x)
- MySQL Driver: mysql:mysql-connector-java → com.mysql:mysql-connector-j
- Flyway: 5.x → 10.x (managed by Boot 3.3.x)
- Lombok: ensure ≥ 1.18.34 (Java 22 compatible)
- Maven Compiler Plugin: 3.8.1 → 3.11.x; <release>22</release>
- Surefire/Failsafe: 2.22.2 → 3.2.5
- Spring Boot Maven Plugin: 2.1.8.RELEASE → 3.3.x
- Tests: JUnit 4 → JUnit 5 (Jupiter)

## Verification Commands

- Effective POM (to verify current resolutions): mvn -q help:effective-pom
- Dependency tree (verbose, to see managed versions): mvn -q dependency:tree -Dverbose
- After upgrade: mvn -q -DskipTests clean verify; then run application locally and smoke test endpoints and views.

