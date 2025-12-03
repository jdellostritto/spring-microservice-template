# Kubernetes Liveness and Readiness Probes

This document describes best practices for configuring Kubernetes probes using Spring Boot Actuator endpoints.

## Overview

Kubernetes uses two types of health checks:

- **Liveness Probe** - Determines if a pod should be restarted
- **Readiness Probe** - Determines if a pod is ready to serve traffic

Spring Boot Actuator provides dedicated health endpoints optimized for each probe type.

## Spring Boot Actuator Endpoints

Spring Boot exposes several health endpoints via the Actuator:

```
GET /actuator/health                    # Default health check (aggregated)
GET /actuator/health/liveness           # Liveness probe endpoint
GET /actuator/health/readiness          # Readiness probe endpoint
GET /actuator/health/live                # Alternative liveness endpoint
GET /actuator/health/ready               # Alternative readiness endpoint
```

### Endpoint Differences

| Endpoint | Purpose | Returns | Use Case |
|----------|---------|---------|----------|
| `/health/liveness` | Pod restart decision | `UP` / `DOWN` | Kubernetes liveness probe |
| `/health/readiness` | Traffic routing decision | `UP` / `DOWN` | Kubernetes readiness probe |
| `/health` | General health status | `UP` / `DOWN` | Monitoring, debugging |

## Best Practices

### 1. Liveness Probe (`/actuator/health/liveness`)

**Purpose:** Detect if the pod is stuck or in a bad state and needs restart.

**Characteristics:**
- Should return `UP` if the pod is alive
- Should not fail due to temporary issues (network timeouts, dependencies down)
- Minimal dependencies checked
- Returns quickly (< 1 second)

**Configuration:**
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8700
  initialDelaySeconds: 90    # Wait 90s for startup
  periodSeconds: 60          # Check every 60s
  timeoutSeconds: 5          # Allow 5s for response
  failureThreshold: 3        # Restart after 3 failures
```

**What it checks:**
- JVM is running
- Spring context is initialized
- Does NOT check: databases, message queues, external services

### 2. Readiness Probe (`/actuator/health/readiness`)

**Purpose:** Determine if pod can handle traffic.

**Characteristics:**
- Returns `UP` if all critical dependencies are ready
- Fails temporarily if dependencies are unavailable
- Includes database, cache, and other critical services
- Kubernetes routes traffic only to `UP` pods

**Configuration:**
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8700
  initialDelaySeconds: 30    # Check after 30s startup
  periodSeconds: 10          # Check frequently (10s)
  timeoutSeconds: 5          # Allow 5s for response
  failureThreshold: 3        # Out of service after 3 failures
```

**What it checks:**
- Database connectivity
- Cache availability
- Message queue connectivity
- External service availability
- Custom business logic readiness

## Configuration in Spring Boot

### Enable Detailed Health Information

In `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      enabled: true
      show-details: when-authorized  # or always for debugging
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

### Add Custom Health Indicators

For readiness probe to check database connectivity:

```java
package com.flipfoundry.tutorial.application.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final JdbcTemplate jdbcTemplate;
    
    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public Health health() {
        try {
            // Quick database connectivity check
            Integer result = jdbcTemplate.queryForObject(
                "SELECT 1", 
                Integer.class
            );
            return Health.up()
                .withDetail("database", "connected")
                .withDetail("query", "SELECT 1")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "connection failed")
                .withException(e)
                .build();
        }
    }
}
```

### Profile-Specific Health Configuration

In `application-deploy.yml` (production):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      enabled: true
      show-details: when-authorized  # Don't expose internal details
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
    diskSpace:
      enabled: false  # Disable expensive checks
    defaults:
      enabled: true
```

## Kubernetes Probe Configuration

### Complete Deployment Example

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-microservice
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-microservice
  template:
    metadata:
      labels:
        app: spring-microservice
    spec:
      containers:
      - name: app
        image: ghcr.io/jdellostritto/spring-microservice-template:master
        ports:
        - containerPort: 8700
          name: http
        
        # ========== LIVENESS PROBE ==========
        # Restarts the pod if it gets stuck
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8700
            scheme: HTTP
          initialDelaySeconds: 90    # JVM startup time + warmup
          periodSeconds: 60          # Check every 60 seconds
          timeoutSeconds: 5          # Wait 5s for response
          successThreshold: 1        # 1 success = healthy
          failureThreshold: 3        # 3 failures = kill pod
        
        # ========== READINESS PROBE ==========
        # Routes traffic based on readiness
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8700
            scheme: HTTP
          initialDelaySeconds: 30    # Wait for dependencies to initialize
          periodSeconds: 10          # Check frequently
          timeoutSeconds: 5          # Wait 5s for response
          successThreshold: 1        # 1 success = ready
          failureThreshold: 3        # 3 failures = not ready
        
        # ========== STARTUP PROBE ==========
        # Optional: For slow-starting applications
        startupProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8700
          initialDelaySeconds: 0
          periodSeconds: 10
          failureThreshold: 30       # Max 5 minutes startup time
        
        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 500m
            memory: 1Gi
        
        environment:
        - name: SPRING_PROFILES_ACTIVE
          value: "deploy"
```

## Response Examples

### Healthy Liveness Response (UP)

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "UP"
}
```

### Unhealthy Liveness Response (DOWN)

```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/json

{
  "status": "DOWN"
}
```

### Healthy Readiness Response (All dependencies UP)

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "version": "13.4"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 53687091200,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

### Partial Readiness Response (Some dependencies down)

```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/json

{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused: No available servers"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

## Port Configuration

### Single Port (Recommended for Docker)

For containerized deployments, expose health probes on the main application port:

```yaml
server:
  port: 8700

management:
  endpoints:
    web:
      base-path: /actuator
```

Probes use: `http://localhost:8700/actuator/health/liveness`

### Separate Management Port (Alternative)

For advanced use cases, use a separate management port:

```yaml
server:
  port: 8700

management:
  server:
    port: 9000
  endpoints:
    web:
      base-path: /actuator
```

Probes use: `http://localhost:9000/actuator/health/liveness`

**Pros:** Firewall can restrict management port
**Cons:** Extra port to expose, more complexity

## Troubleshooting

### 1. Probe Constantly Fails

**Check:**
```bash
# Direct test
curl -v http://localhost:8700/actuator/health/liveness
curl -v http://localhost:8700/actuator/health/readiness

# View pod events
kubectl describe pod <pod-name>

# View logs
kubectl logs <pod-name>
```

**Common issues:**
- Port mismatch in probe configuration
- Endpoint not exposed in management settings
- Application startup taking longer than `initialDelaySeconds`

### 2. Readiness Probe Stuck in NOT READY

**Likely cause:** Dependency (database, cache) is unavailable

**Fix:**
```bash
# Check pod events
kubectl describe pod <pod-name>

# Check logs for dependency errors
kubectl logs <pod-name> | grep -i error

# Verify dependency is running
kubectl get svc <dependency-name>
```

### 3. Liveness Probe Killing Pod Repeatedly

**Likely cause:** Application stuck or memory leak

**Fix:**
- Increase `initialDelaySeconds` and `periodSeconds`
- Reduce `failureThreshold`
- Check application logs for deadlocks or memory issues

## Testing Locally

### Docker Compose Health Check

```yaml
services:
  app:
    image: spring-microservice-template:latest
    ports:
      - 8700:8700
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8700/actuator/health/readiness"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 40s
```

### Manual Testing

```bash
# Test liveness
curl -i http://localhost:8700/actuator/health/liveness

# Test readiness
curl -i http://localhost:8700/actuator/health/readiness

# Full health info
curl -i http://localhost:8700/actuator/health
```

## References

- **Spring Boot Actuator** - [Official Documentation](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- **Kubernetes Probes** - [Official Documentation](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- **Health Indicators** - [Spring Boot Docs](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html#actuator.endpoints.health)
- **Best Practices** - [Kubernetes Health Checks](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#define-startup-probes)
