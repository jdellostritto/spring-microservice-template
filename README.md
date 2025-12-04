# Spring Microservice Template

A production-ready Spring Boot microservice template with enterprise-grade patterns, quality gates, containerization, and comprehensive CI/CD integration.

> **Quick Links:** [🚀 Getting Started](#quick-start) • [📚 Conventions](#conventions) • [🔧 Setup](#setup) • [📖 Documentation](#documentation)

## Overview

This project demonstrates enterprise conventions and patterns for building scalable, maintainable microservices:

✅ **API Versioning** - Content-negotiation with custom media types  
✅ **OpenAPI/Swagger** - Auto-generated API specs published to GitHub Releases  
✅ **Javadocs** - Published to GitHub Pages  
✅ **Code Quality** - SonarQube integration for continuous quality gates  
✅ **Test Coverage** - JaCoCo with comprehensive test suites  
✅ **Logging** - Profile-based, structured logging with correlation IDs  
✅ **Containerization** - Docker support via Jib for optimized image builds  
✅ **CI/CD** - Complete GitHub Actions workflows for builds, releases, and deployment  

---

## Quick Start

### Prerequisites

- Java 21
- Gradle 9.2.0
- Docker (optional, for container builds)

### Installation

```bash
# Clone the repository
git clone https://github.com/jdellostritto/spring-microservice-template.git
cd spring-microservice-template

# Build the project
make build

# Run locally
make bootrun
```

**Access the application:**

- Application: `http://localhost:8700`
- Swagger UI: `http://localhost:8700/test/index.html`
- Actuator: `http://localhost:8700/actuator/health`

---

## Conventions

This project implements enterprise conventions for consistency and clarity. Click on each section for detailed documentation:

| Convention | Purpose | Learn More |
|-----------|---------|-----------|
| **Logging** | Structured, profile-based logging with correlation IDs | [📋 Logging Guide](./docs/LOGGING.md) |
| **API Versioning** | Content-negotiation with custom media types | [📋 API Versioning Guide](./docs/API-VERSIONING.md) |
| **Package Structure** | Organized hierarchy for scalability | [📋 Package Structure Guide](./docs/PACKAGE-STRUCTURE.md) |
| **URI Conventions** | Standardized `/flip/{resource}/` paths | [📋 URI Conventions Guide](./docs/URI-CONVENTIONS.md) |
| **Deprecation Strategy** | Clear migration paths for API changes | [📋 Deprecation Guide](./docs/DEPRECATION.md) |

---

## Available Commands

### Build & Test

```bash
make build          # Build the project with tests
make test           # Run test suite
make clean          # Clean build artifacts
make check          # Run code quality checks
```

### Documentation

```bash
make javadoc        # Generate Javadocs (publishes to GitHub Pages)
make openapi        # Extract OpenAPI spec (publishes to GitHub Releases)
```

### Run & Deploy

```bash
make bootrun        # Run Spring Boot application locally
make dbuild_local   # Build Docker image locally
make dbuild_registry  # Build and push Docker image to registry
```

### Analysis

```bash
make sonar          # Run SonarQube analysis
```

---

## Documentation

### 📖 Full Documentation

Detailed guides for all conventions and integrations:

- **[Logging Conventions](./docs/LOGGING.md)** - Profile-based configuration, log formats, appender strategies
- **[API Versioning](./docs/API-VERSIONING.md)** - Content-negotiation, media types, version management
- **[Package Structure](./docs/PACKAGE-STRUCTURE.md)** - Recommended package hierarchy and organization
- **[URI Conventions](./docs/URI-CONVENTIONS.md)** - RESTful path patterns and naming
- **[Deprecation Strategy](./docs/DEPRECATION.md)** - How to deprecate and evolve APIs
- **[Custom Metrics](./docs/CUSTOM_METRICS.md)** - Micrometer-based custom metrics, AOP recording, Prometheus integration
- **[cURL Testing](./docs/CURL-TESTING.md)** - Testing endpoints with cURL examples
- **[GitHub Actions Setup](./docs/GITHUB_ACTIONS_SETUP.md)** - CI/CD pipeline configuration and automation

### 🔗 External Resources

- **Swagger UI (Local)**: `http://localhost:8700/test/index.html`
- **OpenAPI Spec (GitHub Releases)**: [Latest Release](https://github.com/jdellostritto/spring-microservice-template/releases/latest)
- **Javadocs (GitHub Pages)**: [GitHub Pages Site](https://jdellostritto.github.io/spring-microservice-template/)
- **SonarQube Dashboard**: [SonarCloud Project](https://sonarcloud.io/project/overview?id=jdellostritto_spring-microservice-template)

---

## CI/CD Workflows

All CI/CD workflows are automated via GitHub Actions:

| Workflow | Trigger | Output |
|----------|---------|--------|
| **Build & Test** | Push to any branch | Test results, coverage reports |
| **SonarQube Analysis** | Push to master | Code quality gates, metrics |
| **Javadoc Generation** | Push to master | Published to GitHub Pages |
| **OpenAPI Publishing** | Push to master | Released on GitHub Releases |
| **Docker Build** | Push to master | Container image to registry |

---

## Setup

### Prerequisites

```shell
gradle wrapper --gradle-version 9.2.0
```

### Project Structure

```text
spring-microservice-template/
├── src/
│   ├── main/
│   │   ├── java/com/flipfoundry/tutorial/application/
│   │   │   ├── web/controller/           # REST Controllers
│   │   │   ├── web/dto/                  # Data Transfer Objects
│   │   │   ├── config/                   # Spring Configuration
│   │   │   └── utils/                    # Utilities
│   │   └── resources/
│   │       ├── application.yml           # Main configuration
│   │       ├── application-*.yml         # Profile configs
│   │       └── logback-spring.xml        # Logging config
│   └── test/
│       ├── java/                         # Integration tests
│       └── resources/
└── .github/workflows/                    # CI/CD workflows
    ├── build.yml                         # Build & test
    ├── sonar.yml                         # Code quality
    ├── javadoc.yml                       # Javadoc publishing
    └── openapi.yml                       # OpenAPI publishing
```

### Key Technologies

- **Spring Boot 3.5.7** - Modern Spring framework
- **Java 21** - Latest LTS version
- **Gradle 9.2.0** - Dependency management and build
- **Springdoc OpenAPI 2.6.0** - Automatic API documentation
- **JaCoCo** - Code coverage
- **SonarQube** - Code quality analysis
- **Jib** - Container image building
- **Docker** - Containerization

---

## Contributing

This project demonstrates enterprise patterns and conventions. When contributing:

1. Follow the established [conventions](#conventions)
2. Maintain test coverage above 80%
3. Ensure SonarQube quality gates pass
4. Update relevant documentation

---

## License

Apache 2.0 - See LICENSE file for details

---

## Support

For questions or issues:

- 📝 Open an issue on GitHub
- 💬 Check the [Documentation](#documentation) section
- 🔍 Review the code examples in `src/`
