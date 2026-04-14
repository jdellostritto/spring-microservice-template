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

## Purpose & Audience

This template is a **starting point for Java teams building cloud-native microservices**. It eliminates the boilerplate of setting up a production-grade service from scratch by providing a working reference implementation with:

- Reactive, non-blocking HTTP via Spring WebFlux
- Layered, domain-organized package structure
- Out-of-the-box observability: metrics, distributed tracing, and structured logs
- Kubernetes-ready health probes and graceful shutdown
- Versioned REST API with content negotiation
- Async Kafka event publishing via a reusable sub-module
- Automated quality gates: SonarCloud, JaCoCo coverage, CI/CD pipelines

The example domain is a **Greeting Service** — a deliberately simple use case that keeps the business logic out of the way so the infrastructure patterns remain the focus. To use this as a base for a real service, replace the greeting domain with your own (see [Customizing the Template](#customizing-the-template)).

---

## Architecture

```text
┌─────────────────────────────────────────────────────┐
│                   HTTP Clients                      │
└────────────────────┬────────────────────────────────┘
                     │ :8700
         ┌───────────▼───────────────────┐
         │   Spring WebFlux (Netty)      │
         │   GreetingController          │  ◄── versioned, content-negotiated
         │   DepartingController         │
         └───────┬───────────────┬───────┘
                 │               │
     ┌───────────▼───┐   ┌───────▼──────────────┐
     │ Domain Service │   │ GreetingEventPublisher│──► Kafka (Protobuf)
     └───────┬───────┘   └──────────────────────┘
             │
     ┌───────▼────────────────┐
     │ GreetingPersistenceService│
     └───────┬────────────────┘
             │
     ┌───────▼──────────┐
     │    PostgreSQL     │
     └──────────────────┘

:9001 ──► Actuator (health, readiness, liveness, prometheus, info)
          │
          └──► Grafana Alloy ──► Prometheus · Tempo · Loki · Grafana
```

**Layer responsibilities:**

| Layer | Package | Responsibility |
|---|---|---|
| Web | `web/controller/` | HTTP endpoints, request validation, response mapping |
| DTO | `web/dto/` | API contract types (versioned) |
| Exception handling | `web/exception/` | Global error handling and error response shaping |
| Service | `dal/service/` | Business logic and persistence orchestration |
| Repository | `dal/repository/` | Spring Data R2DBC / JPA repository interfaces |
| Entity | `dal/entity/` | Database-mapped domain objects |
| Events | `events/` | Reactive Kafka event publishing |
| Observability | `observability/` | Micrometer metrics via AOP, OpenTelemetry config |
| Config | `config/` | WebFlux, OpenAPI, and application configuration |

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

**Prerequisites:** Java 21, Docker, Docker Compose, Make

```bash
git clone https://github.com/jdellostritto/spring-microservice-template.git
cd spring-microservice-template
make bootrun
```

Verify the service is running:

```bash
# Greeting endpoint (v1)
curl -H "Accept: application/vnd.flipfoundry.greeting.v1+json" \
     "http://localhost:8700/flip/greeting/greet?name=Alice"

# Health check
curl http://localhost:9001/actuator/health
```

| URL | Purpose |
|---|---|
| `http://localhost:8700` | Application |
| `http://localhost:8700/test/index.html` | Swagger UI |
| `http://localhost:9001/actuator/health` | Health |
| `http://localhost:9001/actuator/prometheus` | Metrics |

---

## Project Structure

```text
spring-microservice-template/
├── src/
│   ├── main/java/com/flipfoundry/tutorial/application/
│   │   ├── config/             # WebFlux and OpenAPI configuration
│   │   ├── dal/                # Data access: entities, repositories, persistence services
│   │   ├── events/             # Kafka event publishing
│   │   ├── observability/      # Micrometer metrics (AOP-driven)
│   │   ├── utils/              # Shared utilities
│   │   └── web/                # Controllers, DTOs, exception handlers
│   └── test/                   # Unit tests (Mockito, WebTestClient)
├── modules/
│   └── kafka-producer/         # Reusable reactive Kafka producer (Protobuf schemas)
├── docs/                       # Detailed convention documentation
├── infra/                      # Docker Compose, observability stack config
├── bin/                        # Helper scripts
├── build.gradle                # Dependency declarations
├── Makefile                    # Build and operations targets
└── compose.yaml                # Full local stack (Kafka, Postgres, Grafana, Loki, Tempo)
```

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

## Testing

```bash
make test          # Unit tests
make build         # Compile + unit tests + JaCoCo coverage report
```

Tests use Mockito for unit-level isolation and `WebTestClient` for controller-layer tests. Coverage is enforced by JaCoCo and reported to SonarCloud on every CI run.

---

## Customizing the Template

To adapt this template for a real microservice:

1. **Rename the root package** from `com.flipfoundry.tutorial` to your organization's namespace
2. **Replace the greeting domain** — remove or repurpose `GreetingController`, `GreetingEntity`, `GreetingRepository`, and their supporting types
3. **Update `application.yml`** with your service name, port, and datasource coordinates
4. **Add your business logic** following the existing layer conventions (see [Package Structure](./docs/PACKAGE-STRUCTURE.md))
5. **Enable or disable Kafka** via the `kafka` Spring profile — the producer module is profile-gated and can be excluded entirely if not needed
6. **Update the OpenAPI config** in `config/openapi/OpenApiProps.java` with your API title and description

---

## Why This Stack?

| Technology | Reason |
|---|---|
| **Spring WebFlux** | Non-blocking I/O handles high concurrency on a small thread pool — correct default for I/O-bound microservices |
| **Reactor Kafka** | Integrates natively with the reactive pipeline; no thread-blocking on publish |
| **Protobuf** | Schema-enforced, compact event payloads; Kafka consumer teams get a versioned contract |
| **OpenTelemetry** | Vendor-neutral auto-instrumentation; traces flow through to Tempo without code changes |
| **Jib** | Reproducible, daemon-free container builds from Gradle; no `Dockerfile` maintenance |
| **SonarCloud + JaCoCo** | Quality gate enforced in CI; prevents coverage regressions from reaching `master` |

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

## 12-Factor App Compliance

This template satisfies all twelve factors from [The Twelve-Factor App](https://12factor.net/) methodology, making it deployable on any cloud platform, Kubernetes cluster, or CI/CD pipeline without modification.

| Factor | Implementation |
|---|---|
| **I. Codebase** | Single Git repository; one codebase, many deploys. Environment differences are driven entirely by Spring profiles — the artifact never changes between dev, staging, and production. |
| **II. Dependencies** | All runtime and build dependencies are declared explicitly in `build.gradle` with pinned versions. Jib packages the full JDK runtime into a self-contained OCI image — no implicit host-level dependencies. |
| **III. Config** | Environment-specific values (`SERVER_PORT`, Kafka brokers, datasource URLs, secrets) are injected via environment variables or Kubernetes `ConfigMap`/`Secret`. `application.yml` defines safe defaults; nothing environment-specific is committed to the repo. |
| **IV. Backing Services** | Kafka, databases, and observability backends (Prometheus, Loki, Tempo) are attached resources referenced by URL and credentials from config. Swapping or re-pointing a service requires only an environment variable change — no code change. |
| **V. Build, Release, Run** | Gradle produces a reproducible JAR (build) → Jib assembles a tagged Docker image (release) → the image runs unmodified in any environment (run). GitHub Actions enforces this pipeline; no mutations occur at startup. |
| **VI. Processes** | The application is stateless by design. The Spring WebFlux reactive pipeline holds no in-memory session or affinity state. Multiple instances run concurrently without coordination or shared local storage. |
| **VII. Port Binding** | Spring Boot's embedded Netty exports HTTP on a configurable port (`SERVER_PORT`, default `8700`). The Actuator management port (`9001`) is separately bound. No external container or application server is required. |
| **VIII. Concurrency** | The reactive (WebFlux) model achieves high concurrency on a minimal thread pool. Horizontal scaling is done by adding container replicas behind a load balancer — the application requires no changes to scale out. |
| **IX. Disposability** | `server.shutdown=graceful` drains in-flight requests before the process exits. Jib's layered image format enables fast cold-starts. Reactive non-blocking I/O eliminates thread-per-request warm-up overhead. |
| **X. Dev/Prod Parity** | `compose.yaml` runs the full observability stack (Kafka, Prometheus, Grafana, Loki, Tempo) locally with the same Docker image used in production. Spring profiles prevent environment-specific code paths from diverging. |
| **XI. Logs** | Logback emits all output to `stdout` as structured text with UTC timestamps and MDC `traceId` correlation. No log files are written to disk. Grafana Alloy ships the log stream to Loki for aggregation, querying, and alerting. |
| **XII. Admin Processes** | Administrative and operational tasks are surfaced through Spring Boot Actuator (`/actuator/health`, `/actuator/prometheus`, `/actuator/info`, `/actuator/metrics`) on a dedicated management port, isolating operational traffic from application traffic. |

### DevOps Compatibility Summary

| Concern | Capability |
|---|---|
| **Container-native** | Jib builds OCI images without a Docker daemon; no `Dockerfile` required for CI |
| **Kubernetes-ready** | Dedicated `/liveness` and `/readiness` probe endpoints; graceful shutdown support; no sticky sessions |
| **Observable by default** | Prometheus metrics at `/actuator/prometheus`; distributed traces via OpenTelemetry → Tempo; logs → Loki |
| **Zero-downtime deploys** | Graceful shutdown + readiness probe ensures the load balancer routes away before the container stops |
| **Secret management** | All credentials are environment-variable-driven; compatible with Vault, AWS Secrets Manager, and Kubernetes Secrets |
| **Reproducible builds** | Gradle with a pinned wrapper version and locked dependency versions; Jib digest-tagged images |
| **Automated quality gates** | SonarCloud quality gate and JaCoCo coverage enforced in CI before any merge to `master` |

---

## Stack

- **Spring Boot 3.5.8** · Java 21 · Gradle 9.2 · Spring WebFlux (reactive)
- **Jib** container builds → GitHub Container Registry (GHCR)
- **Kafka** (Apache KRaft) · Reactive producer (`reactor-kafka`) · **Protobuf 3** event schemas
- **OpenTelemetry** · Grafana Alloy · Prometheus · Grafana · Tempo · Loki
- **JaCoCo** coverage · SonarCloud quality gates · Springdoc OpenAPI

---

**[Javadocs](https://jdellostritto.github.io/spring-microservice-template/)** &nbsp;·&nbsp; **[SonarCloud](https://sonarcloud.io/project/overview?id=jdellostritto_spring-microservice-template)** &nbsp;·&nbsp; **[Latest Release](https://github.com/jdellostritto/spring-microservice-template/releases/latest)**
