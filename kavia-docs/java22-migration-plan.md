# Java 8 to Java 22 Migration: Phased Plan and Checkpoints

## Overview

This plan lays out a sequence of verifiable steps to upgrade the application from Java 8 (Spring Boot 2.1.8) to Java 22 (Spring Boot 3.3.x), including checkpoints, risk mitigation, and rollback.

## Phase 0 — Baseline and Branch (Day 0)

Activities:
- Create a long-lived migration branch (e.g., feature/java22-upgrade).
- On Java 8 toolchain, run:
  - mvn -q -DskipTests clean verify
  - ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=3001,--server.address=0.0.0.0
- Capture current behavior and smoke tests (home, discover, login, cursos pages; H2 console).

Checkpoint:
- Current main branch is green; baseline artifacts archived (test report, packaged jar).

Rollback:
- None needed; this is baseline.

## Phase 1 — Tooling: JDK 22 and Maven (Day 1)

Activities:
- Ensure JDK 22 installed on dev and CI agents.
- Update Maven Wrapper to 3.9.x:
  - mvn -N wrapper -Dmaven=3.9.6
- Add .mvn/toolchains.xml to pin JDK 22 (see implementation document).
- Verify toolchain activation: mvn --version shows Java 22.

Checkpoint:
- mvn -q -DskipTests clean verify completes under Java 22 (with current code), or clearly fails due to Java level. Proceed to Phase 2 once toolchain is effective.

Rollback:
- Revert wrapper update; remove toolchains file if necessary.

## Phase 2 — Spring Boot BOM and Plugins (Day 2)

Activities:
- Update parent to Spring Boot 3.3.x.
- Set java.version 22 or maven.compiler.release 22.
- Update Maven plugins:
  - maven-compiler-plugin 3.11.x (release 22).
  - maven-surefire-plugin 3.2.5 (+ maven-failsafe-plugin if needed).
  - spring-boot-maven-plugin 3.3.x.
- Update dependency coordinates where needed (thymeleaf-extras-springsecurity6, com.mysql:mysql-connector-j).

Checkpoint:
- Project compiles (may still fail due to javax.* or security config incompatibilities; proceed to Phase 3).

Rollback:
- Restore previous pom.xml from baseline.

## Phase 3 — Jakarta Migration (Day 2–3)

Activities:
- Replace javax.persistence.* → jakarta.persistence.* in all entities and persistence usage.
- If javax.validation.* used anywhere, convert to jakarta.validation.*.
- Re-import and fix any minor compilation issues.

Checkpoint:
- Project compiles.
- Unit tests compile.

Rollback:
- Revert code changes; keep pom updates isolated in commits.

## Phase 4 — Spring Security 6 Refactor (Day 3)

Activities:
- Replace WebSecurityConfigurerAdapter with a SecurityFilterChain bean configuration using authorizeHttpRequests and requestMatchers.
- Replace @EnableGlobalMethodSecurity with @EnableMethodSecurity(prePostEnabled = true).
- Configure PasswordEncoder and DaoAuthenticationProvider beans.

Checkpoint:
- Application starts with dev profile; public pages accessible; login works; logout works.

Rollback:
- Revert security configuration class changes.

## Phase 5 — Test Stack Migration (Day 3–4)

Activities:
- Migrate tests to JUnit 5:
  - Replace org.junit.Test with org.junit.jupiter.api.Test.
  - Remove @RunWith(SpringRunner.class).
- Ensure spring-boot-starter-test works with Jupiter and surefire 3.2.5.

Checkpoint:
- mvn -q clean test passes.

Rollback:
- Revert test changes; verify previous surefire settings.

## Phase 6 — Flyway, H2, and MySQL (Day 4)

Activities:
- Verify Flyway migrations on H2 (dev) and MySQL (prod-like).
- If necessary for existing databases, use flyway.baselineOnMigrate=true during the first upgrade (temporary operational flag).
- Validate H2 console works and schema matches expectations.

Checkpoint:
- Migrations run cleanly; dev profile usable; prod config validated.

Rollback:
- Rollback database to pre-upgrade snapshot (ops); revert code changes.

## Phase 7 — Container and CI (Day 4–5)

Activities:
- Update CI to use Java 22 image/runner.
- For Docker: adopt eclipse-temurin:22-jre base or use spring-boot:build-image to produce an OCI image.
- Cache Maven dependencies; ensure repeatable builds.

Checkpoint:
- CI pipeline green; image (if built) runs with Java 22.

Rollback:
- Revert CI changes or pin to previously working image.

## Phase 8 — Final Verification and Sign-Off (Day 5)

Activities:
- Full regression smoke test across key pages: home, discover, cursos list/detail, login/logout, registration.
- Validate authorization rules and secured endpoints.
- Validate templates render correctly with thymeleaf-extras-springsecurity6.

Checkpoint:
- Stakeholder sign-off; merge to main.

## Risks and Mitigations

- Jakarta namespace changes may be incomplete: Use project-wide search and compile to catch all.
- Spring Security DSL differences: Follow the provided template; ensure static resources and public endpoints work.
- H2 2.x differences: If SQL issues arise, consider dev URL tweak ;MODE=MySQL.
- Test migration: Ensure Jupiter annotations are used consistently.

## Rollback Strategy

- Each phase should be a separate commit or PR.
- To rollback, revert the most recent phase commit(s) and restore to last green state.
- For production databases, use snapshots and Flyway repair/baseline procedures where applicable.
