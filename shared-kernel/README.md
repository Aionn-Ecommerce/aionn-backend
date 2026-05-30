# Shared Kernel (`:shared-kernel`)

Shared building blocks for the `aionn-backend` modular monolith (Spring Boot 3.5.x, Java 21, hexagonal + DDD): domain primitives, a standard API response/error contract, and a few common Spring beans. Built with `java-library` + `java-test-fixtures`.

Most of the kernel is plain Java — `Money.of(...)`, `PhoneNumber.of(...)`, `IdGenerator.ulid()` work without any `ApplicationContext`. The Spring beans are picked up automatically by component scan (`app` scans `com.aionn`), so consumers just import what they need.

## Dependency

```groovy
dependencies {
    implementation project(':shared-kernel')
    testImplementation(testFixtures(project(':shared-kernel')))
}
```

Framework deps (`jakarta.*`, `spring-*`) are `compileOnly` here, so they are not forced transitively onto consumers. Jackson is `api` (used by `ApiResponse`/`JacksonConfig`).

## What's inside

- **Domain primitives (pure Java):** `Money`, `PhoneNumber`, `OffsetPagination`, `SortDirection`, `BaseId`, `Guard`, `IdGenerator`, `SlugUtils`, `IpAddressValidator`, `OtpGenerator`, `AggregateRoot`, `DomainEvent`, `EventEnvelope`, `Entity`, `EventPublisher` (port), `Command`/`Query`/`*UseCase`, and the `DomainException` family.
- **Web contract:** `ApiResponse`, `PageMetadata`, `AbstractModuleExceptionHandler`, `GlobalExceptionHandler`, `@ClientIp`.
- **Common Spring beans (component-scanned):** `JacksonConfig` (`@Primary ObjectMapper`), `AuditingConfig` (`@EnableJpaAuditing`), `SpringEventPublisher`, `ClientIpResolver`, `ClientIpArgumentResolver`.
- **Test fixtures:** `BaseIntegrationTest`, `BaseRepositoryTest`, `FakeEventPublisher`, plus `SharedKernelArchRules` (ArchUnit).

## Exception handling

Each module extends `AbstractModuleExceptionHandler` instead of re-implementing error-code mapping:

```java
@RestControllerAdvice(basePackages = "com.aionn.sample.adapter.rest")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SampleModuleExceptionHandler extends AbstractModuleExceptionHandler {

    public SampleModuleExceptionHandler() {
        registerErrors(HttpStatus.BAD_REQUEST, "SAMPLE_001");
        registerErrors(HttpStatus.NOT_FOUND, "SAMPLE_404");
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle(DomainException ex) {
        return handleException(ex);
    }
}
```

Unregistered or `null` error codes fall back to `422` (override via `setDefaultStatus`). `HIGHEST_PRECEDENCE` makes the module handler win over the kernel `GlobalExceptionHandler` (`LOWEST_PRECEDENCE`). Reference: `com.aionn.identity.adapter.rest.exception.IdentityExceptionHandler`.

## API response

`ApiResponse<T>` (`statusCode`, `message`, `data`, `timestamp`, `paging`, `@JsonInclude(NON_NULL)`) with four factories: `success`, `created`, `error`, `successWithPaging`. Use `PageMetadata.from(pagination, total)` for paged responses.

## Domain events

Record events on an `AggregateRoot`, then drain and publish after persisting:

```java
public record OrderPlaced(String orderId, Instant occurredAt) implements DomainEvent {}

order.place();                          // calls record(new OrderPlaced(...))
eventPublisher.publish(order.pullEvents());
```

`EventPublisher` is the domain port; `SpringEventPublisher` is the runtime implementation (publishes via `ApplicationEventPublisher`). In tests use `FakeEventPublisher`.

## Single source of truth

`PhoneNumber` (`^\+?[0-9]{8,15}$`), `Money` (ISO-4217 + HALF_UP), and ULID (`BaseId` + `IdGenerator`, `^[0-9A-HJKMNP-TV-Z]{26}$`) live only here — reuse them, do not redefine. `SharedKernelArchRules` (run from `:app:test`) fails the build if a module redeclares them outside `com.aionn.sharedkernel`.
