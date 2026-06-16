# Aionn Backend

A Spring Boot e-commerce backend organized as a modular monolith. Modules are isolated under `modules/*` and assembled by the `app/` module into a single deployable.

## Stack

- Java 21, Spring Boot, Spring Security
- PostgreSQL (Flyway migrations), Redis
- Gradle multi-project build

## Modules

`modules/identity`, `modules/ucp`, `modules/catalog`, `modules/inventory`, `modules/ordering`, `modules/payment`, `modules/shipping`, `modules/notification`, `modules/promotion`, `modules/chat`. Cross-cutting types live in `shared-kernel/`.

## Run locally

1. Copy `.env.example` to `.env` and fill in the values your environment needs.
2. Bring up infra:
   ```
   docker compose up -d postgres redis
   ```
3. Run the app:
   ```
   ./gradlew :app:bootRun
   ```

The HTTP server defaults to `${SERVER_PORT}` (see `app/src/main/resources/application.yml`).

## Test accounts

Useful for poking at admin / merchant flows in a fresh database. Password is the same for all three: `TestPass123!@`.

| Role         | Email                    | Username       |
|--------------|--------------------------|----------------|
| Merchant     | merchant_001@aionn.com   | merchant_001   |
| CS Admin     | cs_admin_01@aionn.com    | cs_admin_01    |
| Sys Admin    | sys_admin_01@aionn.com   | sys_admin_01   |

## Layout

```
app/                main entry, security, top-level config
modules/<name>/     domain modules (REST, application, domain, persistence)
shared-kernel/      shared types, ports, errors
gradle/             wrapper
```

## Notes

- Per-module config files (`application-<module>.yml`) are imported by `app/src/main/resources/application.yml`.
- Local-only secrets belong in `.env` and `application-local.yml` — both are gitignored.
