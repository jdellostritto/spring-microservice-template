# Logging Conventions

This project uses Logback with profile-based configuration for environment-specific behavior.

## Overview

- **Three deployment profiles** control logging verbosity based on environment
- **Namespace-filtered logging** separates application logs from framework logs
- **UTC timestamps** ensure consistency across distributed systems
- **Asynchronous appenders** prevent logging from impacting application latency
- **Correlation ID support** enables distributed tracing across microservices
- **Separate error logs** provide dedicated operational visibility
- **Tiered retention** balances compliance needs with storage costs

## Profiles

| Profile | Application Logs | System Logs | File Logging | Use Case |
|---------|-----------------|-----------|--------------|----------|
| `default` | DEBUG | INFO | Yes (30 days) | Local development with detailed troubleshooting |
| `test` | INFO | INFO | No | Testing with reduced noise |
| `deploy` | ERROR | ERROR | No | Production with minimal performance impact |

**Usage:** Set the active profile via `spring.profiles.active` in `application-{profile}.yml`

## Log Format

### Production Format

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

### Development Format

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

## Appender Strategy

| Appender | Purpose | Async | Encoding | Use Cases |
|----------|---------|-------|----------|-----------|
| `consoleAppender` | Production-grade console output | Via wrapper | UTF-8 | All profiles |
| `dailyRollingFileAppender` | Development file logging | Via wrapper | UTF-8 | Default profile only |
| `errorFileAppender` | Dedicated error file (90 days) | Via wrapper | UTF-8 | Default profile for compliance |
| `ASYNC_CONSOLE` | Non-blocking console writes | Yes | - | Wraps consoleAppender |
| `ASYNC_FILE` | Non-blocking file writes | Yes | - | Wraps dailyRollingFileAppender |
| `ASYNC_ERROR_FILE` | Non-blocking error writes | Yes | - | Wraps errorFileAppender |

## Industry Best Practices

### Asynchronous Logging

- All appenders wrapped in `AsyncAppender` to prevent blocking application threads
- Queue size: 512, discarding threshold: 0 (no logs lost)
- Benefits: Prevents latency spikes from I/O operations

### Correlation ID / Distributed Tracing

- Logback MDC pattern: `%X{traceId}` included in all log patterns
- Enables cross-service request tracking in microservices
- Integrates with Spring Cloud Sleuth for automatic propagation

### UTF-8 Encoding

- Explicit `charset="UTF-8"` on all encoders
- Ensures international character support and consistency

### Error Log Separation

- Separate `errorFileAppender` with ThresholdFilter for ERROR-level logs
- 5MB file size limit (vs 2MB for general logs)
- 90-day retention for compliance and operations

### Tiered Retention Strategy

- General logs: 30 days (development needs)
- Error logs: 90 days (compliance/audit)
- Test profile: No file storage (cost optimization)

### Cloud-Native Deployment

- Deploy profile: Console-only output (no file I/O)
- Works seamlessly with Docker/Kubernetes log drivers
- Enables log aggregation with ELK, Splunk, etc.

### Improved Log Readability

- Left-aligned level format `[%-5level]` for visual scanning
- Consistent timestamp format across appenders
- Clear hierarchy of information (time → trace → thread → level → logger)

## Configuration

- **Namespace filtering** - `com.flipfoundry.*` logs handled separately with profile-specific levels, `additivity="false"` prevents duplicate logs
- **Root logger** - Catches all system and third-party library logs not matched by specific loggers
- **File logging** - Automatic daily rollover with size-based backup triggers
- **Error tracking** - Separate error file enables quick operational response

**Location:** `src/main/resources/logback-spring.xml`

## Example Usage

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class GreetingController {
    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);
    
    public void processRequest(String traceId, String request) {
        // Set correlation ID for distributed tracing
        MDC.put("traceId", traceId);
        
        try {
            logger.info("Processing request: {}", request);
            // Your business logic
        } catch (Exception e) {
            logger.error("Error processing request", e);
        } finally {
            MDC.remove("traceId");
        }
    }
}
```

## Configuration Files

- `src/main/resources/logback-spring.xml` - Main Logback configuration
- `src/main/resources/application.yml` - Profile settings
- `src/main/resources/application-default.yml` - Development profile
- `src/main/resources/application-test.yml` - Test profile
- `src/main/resources/application-deploy.yml` - Production profile
