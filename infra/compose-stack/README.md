# Deploy Stack

Full local development stack combining the application, Kafka, and the complete observability pipeline.

## Services

| Service       | URL / Port                          | Purpose                          |
|---------------|-------------------------------------|----------------------------------|
| App           | http://localhost:8700               | Spring Boot application          |
| Actuator      | http://localhost:9001/actuator      | Health & metrics                 |
| Kafka         | localhost:9092                      | Kafka broker (KRaft mode)        |
| Kafka UI      | http://localhost:8090               | Topic inspector                  |
| Alloy         | http://localhost:12345              | OTel collector UI                |
| Prometheus    | http://localhost:9090               | Metrics                          |
| Grafana       | http://localhost:3000               | Dashboards                       |
| Tempo         | internal                            | Distributed tracing              |
| Loki          | internal                            | Log aggregation                  |

## Quick Start

```bash
make deploy-up    # start everything
make deploy-down  # stop everything
```

## Kafka

Runs in KRaft mode (no Zookeeper). Auto topic creation is enabled.

Default topics used by the application:
- `greeting-events`
- `departing-events`

## Observability

Traces, metrics, and logs are all collected via the Grafana Alloy collector and forwarded to Tempo, Prometheus, and Loki respectively. Grafana is pre-configured with all three as datasources.
