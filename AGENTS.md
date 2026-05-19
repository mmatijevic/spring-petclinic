# AGENTS.md

## Cursor Cloud specific instructions

### Overview

Spring PetClinic is a Java/Spring Boot web application (single-module). It uses an in-memory H2 database by default — no external services are required for development.

### Key Commands

| Task | Command |
|------|---------|
| Run dev server | `./mvnw spring-boot:run` (serves at http://localhost:8080) |
| Run tests | `./mvnw test -Dtest='!*MySQL*,!*Postgres*'` (skip Docker-dependent tests) |
| Lint/format check | `./mvnw validate` (runs spring-javaformat + checkstyle) |
| Build JAR | `./mvnw package -DskipTests` |
| Full build + tests | `./mvnw package` |

### Non-obvious Notes

- The `validate` phase runs both `spring-javaformat-maven-plugin` (code format) and `maven-checkstyle-plugin` (nohttp). Both must pass before code can be committed.
- MySQL and PostgreSQL integration tests (`*MySQL*`, `*Postgres*`) require Docker and should be excluded when Docker is unavailable. Use `-Dtest='!*MySQL*,!*Postgres*'` to skip them.
- The H2 console is available at `http://localhost:8080/h2-console` when the app is running (JDBC URL is printed at startup).
- CSS is pre-compiled; only recompile with `./mvnw package -P css` if you edit `.scss` files.
- Java 17+ is required; JDK 21 is available in this environment.
