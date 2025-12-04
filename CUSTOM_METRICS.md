# Custom Metrics Guide

This document describes how custom metrics are implemented and can be extended in the Spring Microservice Template.

## Overview

Custom metrics are implemented using **Micrometer**, Spring Boot's metrics abstraction layer, which automatically integrates with OpenTelemetry. All metrics are exposed via the `/actuator/prometheus` endpoint and collected by the Grafana Alloy collector.

## Architecture

### Components

1. **MetricsRegistry** - Centralized registry for all custom metrics
   - Defines Counter, Gauge, and Timer metrics
   - Provides methods to record metric data
   - Located in `src/main/java/.../observability/MetricsRegistry.java`

2. **MetricsAspect** - AOP aspect for automatic metric recording
   - Uses Spring AOP to intercept controller methods
   - Records request count, duration, and errors
   - Located in `src/main/java/.../observability/MetricsAspect.java`

3. **ObservabilityConfiguration** - Configuration component
   - Initializes metrics after application startup
   - Located in `src/main/java/.../observability/ObservabilityConfiguration.java`

## Supported Metric Types

### 1. Counter
Monotonically increasing value. Used for counting events like requests, errors, etc.

```java
Counter requests = Counter.builder("greeting.requests.total")
    .description("Total number of greeting requests")
    .tag("controller", "greeting")
    .register(meterRegistry);

requests.increment();
requests.increment(5);  // Increment by 5
```

### 2. Timer
Measures latency and frequency of events. Automatically calculates percentiles.

```java
Timer duration = Timer.builder("greeting.requests.duration")
    .description("Duration of greeting requests")
    .tag("controller", "greeting")
    .publishPercentiles(0.5, 0.95, 0.99)  // 50th, 95th, 99th percentiles
    .register(meterRegistry);

duration.record(java.time.Duration.ofMillis(100));
```

### 3. Gauge
Snapshot of a current value. Used for instantaneous measurements.

```java
Gauge.builder("active.requests", () -> activeRequests.get())
    .description("Current number of active requests")
    .register(meterRegistry);
```

## Current Metrics

### Greeting Controller Metrics
- `greeting.requests.total` (Counter) - Total greeting requests
- `greeting.requests.duration` (Timer) - Request duration with percentiles
- `greeting.errors.total` (Counter) - Total greeting errors

### Departing Controller Metrics
- `departing.requests.total` (Counter) - Total departing requests
- `departing.requests.duration` (Timer) - Request duration with percentiles
- `departing.errors.total` (Counter) - Total departing errors

## Viewing Metrics

### 1. Prometheus
Access Prometheus UI at `http://localhost:9090`

Query examples:
```
# Total greeting requests
greeting_requests_total

# Request rate (requests per second)
rate(greeting_requests_total[1m])

# P95 response time
greeting_requests_duration{quantile="0.95"}
```

### 2. Grafana
Access Grafana at `http://localhost:3000` (default credentials: admin/admin)

1. Create a new dashboard
2. Add panels with PromQL queries
3. Example queries:
   ```
   rate(greeting_requests_total[1m])  # Requests per second
   greeting_requests_duration_seconds_max  # Max response time
   ```

### 3. Application Health
Access `/actuator/prometheus` directly:
```bash
curl http://localhost:9001/actuator/prometheus | grep greeting
```

## Adding Custom Metrics

### Step 1: Add metric definition to MetricsRegistry

```java
private Counter myCustomCounter;
private Timer myCustomTimer;

private void initializeCustomMetrics() {
    myCustomCounter = Counter.builder("my.custom.counter")
        .description("Description of my custom metric")
        .tag("feature", "myfeature")
        .register(meterRegistry);
    
    myCustomTimer = Timer.builder("my.custom.timer")
        .description("Duration of my custom operation")
        .tag("feature", "myfeature")
        .publishPercentiles(0.5, 0.95, 0.99)
        .register(meterRegistry);
}

// Ensure initializeCustomMetrics() is called from initializeMetrics()
public void initializeMetrics() {
    initializeGreetingMetrics();
    initializeDepartingMetrics();
    initializeCustomMetrics();  // Add this
}
```

### Step 2: Add recording methods

```java
public void recordMyCustomCounter() {
    myCustomCounter.increment();
}

public void recordMyCustomTimer(long durationMs, boolean success) {
    myCustomTimer.record(java.time.Duration.ofMillis(durationMs));
}
```

### Step 3: Add metrics recording to code

**Option A: Manual recording in controller**
```java
@GetMapping("/my-endpoint")
public Mono<MyResponse> myEndpoint() {
    long startTime = System.currentTimeMillis();
    try {
        // Do work
        long duration = System.currentTimeMillis() - startTime;
        metricsRegistry.recordMyCustomTimer(duration, true);
        return result;
    } catch (Exception e) {
        long duration = System.currentTimeMillis() - startTime;
        metricsRegistry.recordMyCustomTimer(duration, false);
        throw e;
    }
}
```

**Option B: Automatic recording via AOP aspect**
Add a new aspect method in `MetricsAspect`:
```java
@Around("execution(* com.flipfoundry.tutorial.application.web.controller.MyController.*(..))")
public Object recordMyMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
    metricsRegistry.recordMyCustomCounter();
    long startTime = System.currentTimeMillis();
    
    try {
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;
        metricsRegistry.recordMyCustomTimer(duration, true);
        return result;
    } catch (Exception ex) {
        long duration = System.currentTimeMillis() - startTime;
        metricsRegistry.recordMyCustomTimer(duration, false);
        throw ex;
    }
}
```

## Best Practices

1. **Naming Convention**: Use snake_case for metric names with dots for hierarchy
   - Good: `greeting.requests.total`, `departing.errors.total`
   - Bad: `GreetingRequests`, `departingErrorsTotal`

2. **Tags**: Add meaningful tags for filtering and grouping
   - Always add `controller` tag for controller metrics
   - Add `endpoint` tag for specific endpoints
   - Add `error_type` tag for error metrics

3. **Descriptions**: Always provide clear descriptions
   - Helps others understand the metric purpose
   - Appears in Prometheus metadata

4. **Avoid High Cardinality**: Don't use user IDs, request IDs, etc. as tag values
   - Can cause memory issues and Prometheus performance problems
   - Use fixed tag values only

5. **Use Percentiles Wisely**: Don't add too many percentiles
   - 0.5 (median), 0.95, 0.99 are standard
   - More percentiles = more cardinality

## Troubleshooting

### Metrics not appearing in Prometheus

1. Check if metrics are being recorded
   ```bash
   curl http://localhost:9001/actuator/prometheus | grep my_metric
   ```

2. Verify Prometheus scrape config is correct
   ```bash
   curl http://localhost:9090/api/v1/query?query=my_metric
   ```

3. Check Alloy collector logs
   ```bash
   docker logs collector
   ```

### Performance Issues

1. Monitor metric cardinality
   - Use `prometheus_tsdb_metric_chunks_created_total` to track metric creation
   - Excessive cardinality can slow down Prometheus

2. Adjust scrape interval in `prometheus.yml`
   - Default is 30s, can increase to reduce load

## References

- [Micrometer Documentation](https://micrometer.io/)
- [Spring Boot Actuator Metrics](https://spring.io/guides/gs/metrics-monitoring/)
- [OpenTelemetry Instrumentation](https://opentelemetry.io/docs/instrumentation/java/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
