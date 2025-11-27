# Java 22 Migration: Convention Tracker and Coding Guidelines

## Purpose

This document sets conventions to align the codebase with Java 22, Spring Boot 3.3.x, and Spring Security 6 while preserving readability and maintainability. It also records adoption decisions for newer Java features.

## Language Features (Java 17+ and Java 22)

### var Usage
- Use var for local variables when type is obvious from the right-hand side and it improves readability.
- Avoid var when the type is not clear or when method return types should be explicit.

### Records
- Do not convert JPA entities to records. Entities need no-arg constructors and are proxied by JPA/Hibernate.
- Records may be used for simple, immutable, read-only DTOs or API payloads where appropriate. Adopt gradually.

### Sealed Classes
- Not required for current domain model. Consider for constrained hierarchies if introduced later.

### Switch Expressions
- Use modern switch expressions where they reduce verbosity and improve clarity.

### Text Blocks
- Use text blocks (""" ... """) for multi-line strings (e.g., test data, logs, documentation snippets). Avoid hardcoding SQL/HQL in code; keep SQL in Flyway migrations or repositories.

### Optional Handling
- Prefer Optional for absent values at API boundaries in service layers.
- Do not store Optional fields in entities.
- Use orElseThrow, orElseGet thoughtfully; avoid Optional.get().

### Nullity and Validation
- Prefer jakarta.validation constraints (@NotNull, @Size, @Email, etc.) on DTOs and entities as needed.
- Favor constructor validation or method parameter validation with @Validated on components.

## Spring Boot and Spring Framework

### Dependency Management
- Rely on Spring Boot’s BOM (3.3.x) for dependency versions whenever possible.
- Avoid hard-coding versions for managed dependencies.

### Bean Injection
- Prefer constructor injection (current code already follows this in key components like EzLearningUserDetailsService).
- Avoid field injection (@Autowired on fields).

### Configuration Properties
- Keep profile-specific files (application-dev.properties, application-prod.properties).
- Use Boot defaults where possible; specify only necessary overrides.

## Spring Security 6

### Configuration Style
- No WebSecurityConfigurerAdapter. Define a SecurityFilterChain @Bean.
- Use authorizeHttpRequests with requestMatchers.
- Replace @EnableGlobalMethodSecurity with @EnableMethodSecurity(prePostEnabled = true).

### Password Encoding and Authorities
- Use BCryptPasswordEncoder bean (strength 10-12 reasonable; current code uses 11).
- Continue using GrantedAuthoritiesMapper where needed; ensure ROLE_ prefix semantics remain consistent.

## Persistence and JPA

### Jakarta Imports
- Use jakarta.persistence.* imports in all entities and persistence-related classes.

### Entity Design
- Keep @Entity classes with no-arg constructors and appropriate annotations.
- Use lazy relationships carefully and validate N+1 queries through testing.

### SQL Migrations
- Keep Flyway migrations in src/main/resources/db/migration.
- Favor database portability; validate on H2 (dev) and MySQL (prod).

## Logging and Exceptions

### Logging
- Use slf4j (LoggerFactory) per class for logs.
- Avoid logging sensitive data (passwords, tokens).

### Exceptions
- Use runtime exceptions for domain/service errors and map them at web boundaries.
- Consider @ControllerAdvice to centralize exception handling in a later phase.

## Testing

### JUnit 5
- Migrate all tests to use Jupiter (org.junit.jupiter.api.*).
- Remove @RunWith(SpringRunner.class); use @SpringBootTest directly.

### Naming
- Keep clear test names; prefer one logical assertion per test. Use nested tests if needed.

## Thymeleaf and Frontend

### Thymeleaf Extras
- Use thymeleaf-extras-springsecurity6.
- Continue using fragments and static assets structure as-is.

### Templates
- Keep templates under src/main/resources/templates with the current structure. Ensure security dialect resolves correctly.

## Security and URL Patterns

- Change antMatchers to requestMatchers.
- Be explicit about static resource paths and public endpoints in SecurityFilterChain.

## Documentation and Comments

- Use Javadoc for public APIs where appropriate.
- Write concise comments for non-obvious business logic or configuration decisions.

## Summary of Key “Musts”

- Use jakarta.* for JPA and validation.
- Define SecurityFilterChain, not WebSecurityConfigurerAdapter.
- Use JUnit 5 and upgraded Maven plugins.
- Prefer constructor injection and Boot-managed dependency versions.
