# Observability Stack

This directory contains the complete observability infrastructure for the Spring Microservice Template application using Docker Compose. It includes distributed tracing, metrics collection, log aggregation, and visualization.

## Overview

The observability stack consists of:

- **Grafana Alloy** - OpenTelemetry collector that receives traces, metrics, and logs via OTLP
- **Prometheus** - Metrics storage and time-series database
- **Grafana** - Visualization and dashboarding for metrics, traces, and logs
- **Tempo** - Distributed trace storage backend
- **Loki** - Log aggregation system

## Architecture

```
┌─────────────────────────────────────────┐
│  Spring Microservice Template           │
│  (OpenTelemetry Spring Boot Starter)    │
└────────────┬────────────────────────────┘
             │ OTLP (gRPC/HTTP)
             ▼
┌─────────────────────────────────────────┐
│  Grafana Alloy (Collector)              │
│  - OTLP Receiver (4317/4318)            │
│  - Batch Processor                      │
└──┬──────────────┬──────────────┬────────┘
   │              │              │
   ▼              ▼              ▼
Tempo        Prometheus       Loki
(Traces)     (Metrics)        (Logs)
   │              │              │
   └──────────┬───┴──────────┬───┘
              ▼              ▼
         ┌─────────────────────────┐
         │  Grafana (Port 3000)    │
         │  - Dashboards           │
         │  - Trace Queries        │
         │  - Log Exploration      │
         └─────────────────────────┘
```

## Quick Start

### Prerequisites

- Docker and Docker Compose installed
- The Spring Microservice Template built locally

### Step 1: Build the Application Docker Image

Build the Spring Boot application as a Docker image:

```bash
make dbuild-local
```

This uses Jib to build the image and tags it as `spring-microservice-template:latest`.

### Step 2: Start the Observability Stack

Start all services including the application:

```bash
make run
```

This will start:
- Spring Microservice Template (ports 8700, 9001)
- Alloy Collector (ports 4317, 4318, 12345)
- Prometheus (port 9090)
- Grafana (port 3000)
- Tempo (ports 4317, 3200)
- Loki (port 3100)

### Step 3: Access the Services

| Service | URL | Purpose |
|---------|-----|---------|
| Application | http://localhost:8700 | REST API endpoints |
| Actuator | http://localhost:9001/actuator | Health, metrics, prometheus endpoints |
| Prometheus | http://localhost:9090 | Metrics query interface |
| Grafana | http://localhost:3000 | Visualization & dashboarding |
| Alloy UI | http://localhost:12345 | Collector status & debugging |

### Step 4: Stop the Stack

Stop all services:

```bash
make down
```

## Configuration

### Alloy Collector (`collector/config-local.alloy`)

The Alloy configuration defines:

- **OTLP Receiver**: Accepts traces, metrics, and logs on gRPC (4317) and HTTP (4318)
- **Batch Processor**: Optimizes throughput with batching
- **Exporters**:
  - Traces → Tempo
  - Metrics → Prometheus
  - Logs → Loki
- **Prometheus Scrape**: Pulls metrics from the application's `/actuator/prometheus` endpoint

### Prometheus (`prometheus/prometheus.yml`)

Configured to:
- Scrape metrics from Alloy every 30 seconds
- Enable exemplar storage (links metrics to traces)
- Scrape from the Spring application directly via service discovery

### Grafana (`grafana/grafana-datasources.yml`)

Pre-configured with datasources:
- **Prometheus**: http://prometheus:9090
- **Tempo**: http://tempo:3200
- **Loki**: http://loki:3100

## OpenTelemetry Configuration

The application connects to the collector via environment variables set in `docker-compose.local.yml`:

```yaml
OTEL_EXPORTER_OTLP_PROTOCOL: grpc
OTEL_EXPORTER_OTLP_ENDPOINT: http://collector:4317
OTEL_TRACES_EXPORTER: otlp
OTEL_METRICS_EXPORTER: otlp
OTEL_LOGS_EXPORTER: otlp
OTEL_RESOURCE_ATTRIBUTES: "service.name=spring-microservice-template,service.version=0.0.1-SNAPSHOT"
```

These are automatically applied by the OpenTelemetry Spring Boot Starter dependency.

## Generating Observability Data

Once the stack is running, make requests to generate traces, metrics, and logs:

```bash
# Generate greeting requests
curl http://localhost:8700/flip/greeting/greet?name=World

# Generate departing requests
curl http://localhost:8700/flip/departing/depart

# View metrics
curl http://localhost:9001/actuator/metrics

# Prometheus metrics endpoint
curl http://localhost:9001/actuator/prometheus
```

## Viewing Observability Data

### Prometheus

1. Navigate to http://localhost:9090
2. Use the query interface to explore metrics:
   - `http_server_requests_seconds_count` - HTTP request counts
   - `jvm_memory_used_bytes` - JVM memory usage
   - Application-specific metrics from MetricsRegistry

### Grafana

1. Navigate to http://localhost:3000
2. Default credentials: `admin` / `admin`
3. Explore dashboards or create custom ones using:
   - Prometheus for metrics
   - Tempo for traces
   - Loki for logs

### Tempo (Traces)

1. In Grafana, add Tempo as a datasource (already configured)
2. Use the Explore view to query traces
3. Filter by service name, operation, or error status

### Loki (Logs)

1. In Grafana, use Loki datasource in Explore
2. Query logs using LogQL
3. Example: `{service="spring-microservice-template"}`

## Custom Metrics

The application includes custom metrics defined in `src/main/java/.../observability/MetricsRegistry.java`:

- `greeting.requests.total` - Total greeting requests
- `greeting.requests.duration` - Greeting request duration (p50, p95, p99)
- `departing.requests.total` - Total departing requests
- `departing.requests.duration` - Departing request duration

These metrics are automatically recorded via AOP aspects in `MetricsAspect.java` and initialized by `ObservabilityConfiguration.java`.

## Troubleshooting

### Application Not Sending Telemetry

1. **Check OTEL environment variables** in the container:
   ```bash
   docker exec spring-microservice-template env | grep OTEL
   ```

2. **Verify collector is accessible** from the application:
   ```bash
   docker exec spring-microservice-template curl -v http://collector:4317
   ```

3. **Check application logs** for OpenTelemetry errors:
   ```bash
   docker logs spring-microservice-template
   ```

### Collector Not Receiving Data

1. **Check Alloy status**: http://localhost:12345
2. **Verify OTLP endpoint is listening**:
   ```bash
   docker logs collector | grep -i otlp
   ```

### Prometheus Not Scraping Metrics

1. **Check Prometheus targets**: http://localhost:9090/targets
2. **Verify actuator endpoint**:
   ```bash
   curl http://localhost:9001/actuator/prometheus
   ```

## Resource Cleanup

To remove all containers, volumes, and networks:

```bash
make down
docker system prune -f
```

## References

- [OpenTelemetry Spring Boot Starter](https://opentelemetry.io/docs/zero-code/java/spring-boot-starter/)
- [Grafana Alloy Documentation](https://grafana.com/docs/alloy/latest/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/grafana/)
- [Tempo Documentation](https://grafana.com/docs/tempo/)
- [Loki Documentation](https://grafana.com/docs/loki/)
