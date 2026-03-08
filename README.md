# Spring Microservice Template

[![Build and Test](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/build.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/build.yml)
[![SonarQube Analysis](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/sonar.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/sonar.yml)
[![Docker Build](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/docker.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/docker.yml)
[![Javadocs](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/javadoc.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/javadoc.yml)
[![OpenAPI](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/openapi.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/openapi.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=jdellostritto_spring-microservice-template&metric=alert_status)](https://sonarcloud.io/project/overview?id=jdellostritto_spring-microservice-template)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jdellostritto_spring-microservice-template&metric=coverage)](https://sonarcloud.io/project/overview?id=jdellostritto_spring-microservice-template)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

Production-ready Spring Boot microservice template demonstrating enterprise patterns and conventions.

---

## Conventions

| Convention | Summary |
|---|---|
| [API Versioning](./docs/API-VERSIONING.md) | Content-negotiation via `Accept` header — never via URL path segments |
| [URI Design](./docs/URI-CONVENTIONS.md) | RESTful paths under `/flip/{resource}/` namespace, HTTP verbs for operations |
| [Package Structure](./docs/PACKAGE-STRUCTURE.md) | Layered architecture organized by functional domain, not technical layer |
| [Logging](./docs/LOGGING.md) | Async Logback with UTC timestamps, MDC `traceId`, profile-driven verbosity |
| [Custom Metrics](./docs/CUSTOM_METRICS.md) | Micrometer counters/timers via AOP, exposed at `/actuator/prometheus` |
| [Deprecation Strategy](./docs/DEPRECATION.md) | 6-month minimum notice with `@Deprecated`, `Sunset` headers, `forRemoval` flag |
| [Kubernetes Probes](./docs/KUBERNETES-PROBES.md) | Dedicated `/liveness` and `/readiness` endpoints — no infra checks on liveness |
| [CI/CD Workflows](./docs/GITHUB_ACTIONS_SETUP.md) | Five GitHub Actions workflows: build, Sonar, Docker, Javadoc, OpenAPI |
| [Endpoint Testing](./docs/CURL-TESTING.md) | cURL examples for content-negotiated versioned API endpoints |
| [Event / Data Bus](./docs/EVENT-BUS.md) | Reactive Kafka producer module — Protobuf schemas, idempotent delivery, profile-gated activation |

---

## Quick Start

```bash
git clone https://github.com/jdellostritto/spring-microservice-template.git
cd spring-microservice-template
make bootrun
```

| URL | Purpose |
|---|---|
| `http://localhost:8700` | Application |
| `http://localhost:8700/test/index.html` | Swagger UI |
| `http://localhost:9001/actuator/health` | Health |
| `http://localhost:9001/actuator/prometheus` | Metrics |

---

## Commands

| Command | Action |
|---|---|
| `make build` | Compile and run tests |
| `make test` | Run test suite |
| `make clean` | Remove build artifacts |
| `make bootrun` | Start application locally |
| `make dbuild-local` | Build Docker image locally |
| `make up-stack` | Start full observability stack (Docker Compose) |
| `make down-stack` | Stop observability stack |
| `make sonar` | Run SonarQube analysis |
| `make javadoc` | Generate and publish Javadocs |
| `make openapi` | Generate and archive OpenAPI spec |

---

## Workflows

| Workflow | Trigger | Output |
|---|---|---|
| [![Build and Test](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/build.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/build.yml) | Push / PR → `master`, `develop` | Test results, JaCoCo coverage |
| [![SonarQube Analysis](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/sonar.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/sonar.yml) | Push / PR → `master`, `develop` | Quality gate, coverage metrics |
| [![Docker Build](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/docker.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/docker.yml) | Push → `master` | `ghcr.io/jdellostritto/spring-microservice-template` |
| [![Javadocs](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/javadoc.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/javadoc.yml) | Push → `master` | [GitHub Pages](https://jdellostritto.github.io/spring-microservice-template/) |
| [![OpenAPI](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/openapi.yml/badge.svg)](https://github.com/jdellostritto/spring-microservice-template/actions/workflows/openapi.yml) | Push → `master`, `develop` | Artifact: `openapi-spec` |

---

## Stack

- **Spring Boot 3.5.8** · Java 21 · Gradle 9.2 · Spring WebFlux (reactive)
- **Jib** container builds → GitHub Container Registry (GHCR)
- **Kafka** (Apache KRaft) · Reactive producer (`reactor-kafka`) · **Protobuf 3** event schemas
- **OpenTelemetry** · Grafana Alloy · Prometheus · Grafana · Tempo · Loki
- **JaCoCo** coverage · SonarCloud quality gates · Springdoc OpenAPI

---

**[Javadocs](https://jdellostritto.github.io/spring-microservice-template/)** &nbsp;·&nbsp; **[SonarCloud](https://sonarcloud.io/project/overview?id=jdellostritto_spring-microservice-template)** &nbsp;·&nbsp; **[Latest Release](https://github.com/jdellostritto/spring-microservice-template/releases/latest)**
