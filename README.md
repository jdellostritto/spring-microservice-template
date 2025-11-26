# Spring Microservice Template

A comprehensive Spring Boot microservice demonstrating enterprise-grade patterns, quality gates, and containerization.

## Project: api-lifecycle-enterprise

This is the **enterprise/production-ready** version featuring full CI/CD integration, code quality analysis, and containerization support.

Installation step is required:

`gradle wrapper --gradle-version 9.2.0`

### Key Features

This project demonstrates enterprise-grade conventions and patterns:

- **Package hierarchy** - Organized structure following `com.flipfoundry.tutorial.application.*` conventions
- **URI conventions** - Standardized `/flip/{resource}/` paths across the platform
- **API versioning** - Content-negotiation with custom media types (`application/vnd.flipfoundry.{resource}.v{version}+json`)
- **Deprecation strategy** - Clear migration paths using `@Deprecated` annotations and JavaDoc tags
- **Profile-based logging** - Environment-specific behavior (default/test/deploy) with namespace filtering
- **Comprehensive documentation** - JavaDocs, OpenAPI/Swagger integration, and architectural clarity
- **Enterprise tooling** - JaCoCo code coverage, SonarQube analysis, and quality gates
- **Container-ready** - Docker support with Jib for optimized image builds
- **Integration testing** - Full test suites demonstrating best practices

## Conventions

This project follows enterprise conventions across logging, APIs, and packaging for consistency and clarity.

### Quick Reference

| Convention | Details |
|-----------|---------|
| **Logging** | Profile-based (default/test/deploy) with namespace filtering |
| **API Paths** | `/flip/{resource}/` following Flip Foundry conventions |
| **Versioning** | Content-negotiation via Accept headers with media types |
| **Packages** | `com.flipfoundry.tutorial.application.*` hierarchy |
| **Deprecation** | `@Deprecated` annotations with clear migration paths |

---

## Logging Conventions

This project uses Logback with profile-based configuration for environment-specific behavior:

### Summary

- **Three deployment profiles** control logging verbosity based on environment
- **Namespace-filtered logging** separates application logs from framework logs
- **UTC timestamps** ensure consistency across distributed systems
- **Asynchronous appenders** prevent logging from impacting application latency
- **Correlation ID support** enables distributed tracing across microservices
- **Separate error logs** provide dedicated operational visibility
- **Tiered retention** balances compliance needs with storage costs

### Comprehensive Details

#### Profiles

| Profile | Application Logs | System Logs | File Logging | Use Case |
|---------|-----------------|-----------|--------------|----------|
| `default` | DEBUG | INFO | Yes (30 days) | Local development with detailed troubleshooting |
| `test` | INFO | INFO | No | Testing with reduced noise |
| `deploy` | ERROR | ERROR | No | Production with minimal performance impact |

**Usage:** Set the active profile via `spring.profiles.active` in `application-{profile}.yml`

#### Log Format

**Production Format:**

```text
%d{"ISO8601", UTC} [%X{traceId}] [%thread] [%-5level] %logger{5} - %msg
```

Components:

- `%d{"ISO8601", UTC}` - ISO 8601 timestamp in UTC for distributed consistency
- `[%X{traceId}]` - Correlation ID for distributed tracing (MDC)
- `[%thread]` - Thread name for concurrent request tracking
- `[%-5level]` - Left-aligned log level for improved readability
- `%logger{5}` - Logger name abbreviated to 5 characters
- `%msg` - Log message

**Development Format:**

```text
%d{yyyy-MM-dd HH:mm:ss} %p %t %c %M - %m
```

Components:

- `%d{yyyy-MM-dd HH:mm:ss}` - Local timestamp for readability
- `%p` - Priority/level
- `%t` - Thread name
- `%c` - Full class name
- `%M` - Method name
- `%m` - Message

#### Appender Strategy

| Appender | Purpose | Async | Encoding | Use Cases |
|----------|---------|-------|----------|-----------|
| `consoleAppender` | Production-grade console output | Via wrapper | UTF-8 | All profiles |
| `dailyRollingFileAppender` | Development file logging | Via wrapper | UTF-8 | Default profile only |
| `errorFileAppender` | Dedicated error file (90 days) | Via wrapper | UTF-8 | Default profile for compliance |
| `ASYNC_CONSOLE` | Non-blocking console writes | Yes | - | Wraps consoleAppender |
| `ASYNC_FILE` | Non-blocking file writes | Yes | - | Wraps dailyRollingFileAppender |
| `ASYNC_ERROR_FILE` | Non-blocking error writes | Yes | - | Wraps errorFileAppender |

#### Industry Best Practices Implemented

##### Asynchronous Logging

- All appenders wrapped in `AsyncAppender` to prevent blocking application threads
- Queue size: 512, discarding threshold: 0 (no logs lost)
- Benefits: Prevents latency spikes from I/O operations

##### Correlation ID / Distributed Tracing

- Logback MDC pattern: `%X{traceId}` included in all log patterns
- Enables cross-service request tracking in microservices
- Integrates with Spring Cloud Sleuth for automatic propagation

##### UTF-8 Encoding

- Explicit `charset="UTF-8"` on all encoders
- Ensures international character support and consistency

##### Error Log Separation

- Separate `errorFileAppender` with ThresholdFilter for ERROR-level logs
- 5MB file size limit (vs 2MB for general logs)
- 90-day retention for compliance and operations

##### Tiered Retention Strategy

- General logs: 30 days (development needs)
- Error logs: 90 days (compliance/audit)
- Test profile: No file storage (cost optimization)

##### Cloud-Native Deployment

- Deploy profile: Console-only output (no file I/O)
- Works seamlessly with Docker/Kubernetes log drivers
- Enables log aggregation with ELK, Splunk, etc.

##### Improved Log Readability

- Left-aligned level format `[%-5level]` for visual scanning
- Consistent timestamp format across appenders
- Clear hierarchy of information (time → trace → thread → level → logger)

#### Configuration Details

- **Namespace filtering** - `com.flipfoundry.*` logs handled separately with profile-specific levels, `additivity="false"` prevents duplicate logs
- **Root logger** - Catches all system and third-party library logs not matched by specific loggers
- **File logging** - Automatic daily rollover with size-based backup triggers
- **Error tracking** - Separate error file enables quick operational response

**Location:** `src/main/resources/logback-spring.xml`

---

## API Conventions

### URI Paths

All endpoints follow the pattern: `/flip/{resource}/{action}`

Examples:

- `/flip/greeting/greet` - Primary greeting endpoint
- `/flip/departing/depart` - Departure message endpoint

### Versioning Strategy

Versioning is implemented via **content-negotiation** using Accept headers with custom media types:

```text
Accept: application/vnd.flipfoundry.{resource}.v{version}+json
```

Examples:

- `application/vnd.flipfoundry.greeting.v1+json` - Greeting API version 1
- `application/vnd.flipfoundry.greeting.v2+json` - Greeting API version 2

### Deprecation Approach

Old API versions are marked with:

- `@Deprecated(since = "X.Y", forRemoval = boolean)` - Java deprecation annotation
- `@deprecated` - JavaDoc tag with clear migration instructions
- Consistent error handling with HTTP 406 (Not Acceptable) for unsupported media types

---

## Package Conventions

**Structure:** `com.flipfoundry.tutorial.application.{layer}`

- `web.controller` - REST controllers
- `web.dto` - Data transfer objects
- `service` - Business logic
- `repository` - Data access
