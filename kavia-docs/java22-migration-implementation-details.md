# Java 8 to Java 22 Migration: Implementation Details

## Overview

This document provides concrete, file-level instructions and code examples to perform the migration:
- pom.xml updates for Spring Boot 3.3.x and Java 22
- Maven Toolchains setup
- Spring Security configuration rewrite (Security 6)
- Jakarta namespace changes
- Test migration to JUnit 5
- Profile/property and DB compatibility notes
- Optional Docker and CI snippets

Only documentation is being changed here; apply code changes in the repository as described below.

## 1) pom.xml Updates

Key changes:
- Parent to Spring Boot 3.3.x
- Set java.version to 22 (or maven.compiler.release 22)
- Upgrade plugins and dependencies (thymeleaf extras, MySQL driver)
- Keep dependency versions managed by Boot

Example pom.xml (diff-style excerpt):

```diff
--- a/pom.xml
+++ b/pom.xml
@@
-    <parent>
-        <groupId>org.springframework.boot</groupId>
-        <artifactId>spring-boot-starter-parent</artifactId>
-        <version>2.1.8.RELEASE</version>
-        <relativePath/>
-    </parent>
+    <parent>
+        <groupId>org.springframework.boot</groupId>
+        <artifactId>spring-boot-starter-parent</artifactId>
+        <version>3.3.5</version> <!-- or latest 3.3.x -->
+        <relativePath/>
+    </parent>
@@
-    <properties>
-        <java.version>1.8</java.version>
-    </properties>
+    <properties>
+        <java.version>22</java.version>
+        <maven.compiler.release>22</maven.compiler.release>
+        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
+    </properties>
@@
-        <dependency>
-            <groupId>org.thymeleaf.extras</groupId>
-            <artifactId>thymeleaf-extras-springsecurity5</artifactId>
-        </dependency>
+        <dependency>
+            <groupId>org.thymeleaf.extras</groupId>
+            <artifactId>thymeleaf-extras-springsecurity6</artifactId>
+        </dependency>
@@
-        <dependency>
-            <groupId>mysql</groupId>
-            <artifactId>mysql-connector-java</artifactId>
-        </dependency>
+        <dependency>
+            <groupId>com.mysql</groupId>
+            <artifactId>mysql-connector-j</artifactId>
+        </dependency>
@@
-            <plugin>
-                <groupId>org.apache.maven.plugins</groupId>
-                <artifactId>maven-compiler-plugin</artifactId>
-                <version>3.8.1</version>
-                <configuration>
-                    <source>${java.version}</source>
-                    <target>${java.version}</target>
-                    <release>8</release>
-                    <encoding>UTF-8</encoding>
-                    <annotationProcessorPaths>
-                        <path>
-                            <groupId>org.projectlombok</groupId>
-                            <artifactId>lombok</artifactId>
-                            <version>1.18.28</version>
-                        </path>
-                    </annotationProcessorPaths>
-                </configuration>
-            </plugin>
+            <plugin>
+                <groupId>org.apache.maven.plugins</groupId>
+                <artifactId>maven-compiler-plugin</artifactId>
+                <version>3.11.0</version>
+                <configuration>
+                    <release>22</release>
+                    <encoding>UTF-8</encoding>
+                </configuration>
+            </plugin>
@@
-            <plugin>
-                <groupId>org.apache.maven.plugins</groupId>
-                <artifactId>maven-surefire-plugin</artifactId>
-                <version>2.22.2</version>
-                <configuration>
-                    <useSystemClassLoader>true</useSystemClassLoader>
-                </configuration>
-            </plugin>
+            <plugin>
+                <groupId>org.apache.maven.plugins</groupId>
+                <artifactId>maven-surefire-plugin</artifactId>
+                <version>3.2.5</version>
+            </plugin>
+            <plugin>
+                <groupId>org.apache.maven.plugins</groupId>
+                <artifactId>maven-failsafe-plugin</artifactId>
+                <version>3.2.5</version>
+            </plugin>
@@
-            <plugin>
-                <groupId>org.springframework.boot</groupId>
-                <artifactId>spring-boot-maven-plugin</artifactId>
-                <version>2.1.8.RELEASE</version>
-            </plugin>
+            <plugin>
+                <groupId>org.springframework.boot</groupId>
+                <artifactId>spring-boot-maven-plugin</artifactId>
+            </plugin>
```

Notes:
- With Boot 3 parent, you can omit explicit spring-boot-maven-plugin version; it inherits from the parent.
- Keep Lombok dependency as optional (no need to configure annotationProcessorPaths explicitly with modern plugin/toolchain).
- Rely on Boot BOM for managed dependency versions.

## 2) Maven Toolchains Setup

Create .mvn/toolchains.xml to guarantee JDK 22 usage:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>22</version>
      <vendor>any</vendor>
    </provides>
    <configuration>
      <jdkHome>/path/to/jdk-22</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

- Adjust jdkHome path as appropriate for local and CI environments.
- On CI, prefer environment variables (e.g., $JAVA_HOME_22) injected into toolchains.xml at build time.

## 3) Jakarta Namespace Changes

Update imports in all JPA entities and any persistence code:

Example change in src/main/java/com/ezlearning/platform/auth/User.java:

```diff
-import javax.persistence.*;
+import jakarta.persistence.*;
```

Apply the same replacement in:
- src/main/java/com/ezlearning/platform/model/Curso.java
- src/main/java/com/ezlearning/platform/model/Profesor.java
- src/main/java/com/ezlearning/platform/model/Matricula.java

No functional changes expected beyond imports.

## 4) Spring Security 6 Configuration

Replace WebSecurityConfigurerAdapter with a configuration class that declares SecurityFilterChain and related beans.

Example replacement for src/main/java/com/ezlearning/platform/security/ApplicationSecurityConfiguration.java:

```java
package com.ezlearning.platform.security;

import com.ezlearning.platform.auth.EzLearningUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    public GrantedAuthoritiesMapper authoritiesMapper() {
        SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
        authorityMapper.setConvertToUpperCase(true);
        authorityMapper.setDefaultAuthority("USER");
        return authorityMapper;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            EzLearningUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            GrantedAuthoritiesMapper authoritiesMapper) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setAuthoritiesMapper(authoritiesMapper);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index", "/discover", "/cursos", "/h2-console/**",
                    "/api/**", "/register", "/css/**", "/js/**", "/img/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login").permitAll()
            )
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/logout-success")
                .permitAll()
            );
        return http.build();
    }
}
```

Notes:
- @EnableGlobalMethodSecurity is replaced with @EnableMethodSecurity.
- antMatchers is replaced by requestMatchers; AntPathRequestMatcher remains usable for logout.

## 5) Tests: JUnit 5 Migration

Example change for src/test/java/com/ezlearning/platform/PlatformApplicationTests.java:

```diff
-package com.ezlearning.platform;
-
-import org.junit.Test;
-import org.junit.runner.RunWith;
-import org.springframework.boot.test.context.SpringBootTest;
-import org.springframework.test.context.junit4.SpringRunner;
-
-@RunWith(SpringRunner.class)
-@SpringBootTest
-public class PlatformApplicationTests {
-
-    @Test
-    public void contextLoads() {
-    }
-
-}
+package com.ezlearning.platform;
+
+import org.junit.jupiter.api.Test;
+import org.springframework.boot.test.context.SpringBootTest;
+
+@SpringBootTest
+class PlatformApplicationTests {
+
+    @Test
+    void contextLoads() {
+    }
+}
```

Ensure maven-surefire-plugin is ≥ 3.2.5 and spring-boot-starter-test is managed by Boot 3.3.x.

## 6) Profiles and Properties

application-dev.properties (dev):
- H2 remains with org.h2.Driver and mem: URL, which is compatible with H2 2.x.
- If you encounter SQL compatibility issues with H2 2.x, consider using:
  - spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL
- Keep spring.h2.console.enabled=true for local troubleshooting.

application-prod.properties (prod):
- Driver: com.mysql.cj.jdbc.Driver (already set).
- Update DB_URL/DB_USERNAME/DB_PASSWORD in the environment as before.

application.properties:
- spring.jpa.hibernate.ddl-auto=none remains valid. Rely on Flyway migrations.

## 7) Optional Dockerfile (Example)

Option A: Use Spring Boot buildpacks (no Dockerfile needed):
- mvn -DskipTests spring-boot:build-image

Option B: Dockerfile with Java 22:

```dockerfile
FROM eclipse-temurin:22-jre
WORKDIR /app
COPY target/platform-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Note: Adjust exposed port and jar name as needed.

## 8) CI Pipeline Notes

- Ensure CI uses JDK 22:
  - For GitHub Actions, use actions/setup-java with distribution: temurin and java-version: '22'
- Cache ~/.m2/repository to speed up builds.
- Run:
  - mvn -q -DskipTests clean verify
  - mvn -q test
  - Optionally: mvn -q spring-boot:build-image

## 9) Validation and Smoke Tests

- Start app with dev profile:
  - ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=3001,--server.address=0.0.0.0
- Validate pages:
  - /, /index, /discover, /cursos
  - /login, /logout, /logout-success
  - /h2-console (if enabled)
- Validate basic CRUD flows via UI and repositories.

## 10) Rollback Notes

- Keep each set of changes in separate commits.
- To rollback, revert the last change set and re-run baseline with previous Java/Maven settings.
- For database changes in production, use Flyway’s repair/baseline features and database snapshots.

