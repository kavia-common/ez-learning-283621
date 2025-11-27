# Java 22 Migration Package: Overview and Links

## Documents in this Package

- Dependency Upgrade Map
  - kavia-docs/java22-migration-dependency-upgrade-map.md

- Architecture and Approach
  - kavia-docs/java22-migration-architecture-approach.md

- Convention Tracker (Java 22 + Spring Boot 3)
  - kavia-docs/java22-migration-convention-tracker.md

- Phased Plan and Checkpoints
  - kavia-docs/java22-migration-plan.md

- Implementation Details (file-level changes and examples)
  - kavia-docs/java22-migration-implementation-details.md

## Repository Pointers

- Build file: pom.xml
- Application entry point: src/main/java/com/ezlearning/platform/PlatformApplication.java
- Security configuration: src/main/java/com/ezlearning/platform/security/ApplicationSecurityConfiguration.java (to be replaced per implementation doc)
- Entities (Jakarta migration targets):
  - src/main/java/com/ezlearning/platform/auth/User.java
  - src/main/java/com/ezlearning/platform/model/Curso.java
  - src/main/java/com/ezlearning/platform/model/Profesor.java
  - src/main/java/com/ezlearning/platform/model/Matricula.java
- Tests:
  - src/test/java/com/ezlearning/platform/PlatformApplicationTests.java
- Configuration:
  - src/main/resources/application.properties
  - src/main/resources/application-dev.properties
  - src/main/resources/application-prod.properties
- Migrations:
  - src/main/resources/db/migration/
