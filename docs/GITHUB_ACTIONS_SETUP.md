# GitHub Actions Setup for spring-microservice-template

## Overview

This project uses GitHub Actions workflows that are consolidated with Makefile targets for consistency between local development and CI/CD environments.

## Makefile Targets

The project provides the following Make targets (work on both local development and GitHub Actions):

- **`make build`** - Clean and build the project
- **`make test`** - Build and run tests
- **`make test-report`** - Generate test results summary for GitHub job summary
- **`make sonar`** - Build and run SonarQube/SonarCloud analysis (with token check)
- **`make dbuild_local`** - Build Docker image locally to Docker daemon
- **`make dbuild_registry`** - Build and push Docker image to registry (GHCR, DockerHub, etc.)

## Workflows Configured

### 1. **build.yml** - Build and Test

- **Trigger:** Push to `master`/`develop`, Pull Requests
- **Make targets used:**
  - `make build` - Compile and build
  - `make test` - Run all tests
  - `make test-report` - Generate test summary
- **Actions:**
  - Compile code with Java 21
  - Run all tests (unit + integration)
  - Generate JaCoCo coverage reports
  - Upload test and coverage reports as artifacts
  - Publish test results summary in GitHub job summary

### 2. **sonar.yml** - SonarQube/SonarCloud Analysis

- **Trigger:** Push to `master`/`develop`, Pull Requests
- **Environment:** Requires `BUILDS` environment configured in GitHub
- **Make target used:**
  - `make sonar` - Build and run SonarQube analysis (conditional on SONAR_TOKEN)
- **Actions:**
  - Build project and run SonarQube analysis
  - Skip analysis if SONAR_TOKEN not configured
  - Post SonarCloud dashboard link as PR comment
  - Publish analysis summary in GitHub job summary
- **Configuration:**
  - Disables Gradle configuration cache (`--no-configuration-cache`)
  - Uses `BUILDS` environment for secret isolation
- **Requires GitHub Secrets (in `BUILDS` environment):**
  - `SONAR_TOKEN` - Your SonarCloud authentication token

### 3. **docker.yml** - Docker Build and Push to GHCR

- **Trigger:** Push to `master` branch, manual dispatch (workflow_dispatch)
- **Make target used:**
  - `make dbuild_registry` - Build and push to registry
- **Actions:**
  - Build Docker image using Jib Gradle plugin
  - Push to GitHub Container Registry (GHCR)
  - Tags: `latest`, `master`, and `<commit-sha>`
  - **No secrets required** - uses built-in `GITHUB_TOKEN`
- **Configuration:**
  - Disables Gradle configuration cache (`--no-configuration-cache`)
  - Registry image, tags, and credentials passed via environment variables
- **Image URL:** `ghcr.io/jdellostritto/spring-microservice-template:latest`

### 4. **javadoc.yml** - Build & Publish Javadocs

- **Trigger:** Push to `master`/`develop`, manual dispatch (workflow_dispatch)
- **Actions:**
  - Generate Javadoc HTML documentation
  - Publish to `/docs` directory for GitHub Pages
  - Avoid infinite loops by detecting github-actions[bot] commits
- **Output:**
  - HTML documentation at `https://yourusername.github.io/spring-microservice-template/`
  - Includes class hierarchies, method documentation, cross-references

### 5. **openapi.yml** - Generate & Archive OpenAPI Spec

- **Trigger:** Push to `master`/`develop`, manual dispatch (workflow_dispatch)
- **Make target used:**
  - `make openapi` - Generate OpenAPI specification
- **Actions:**
  - Build project and generate OpenAPI spec via `resolveOpenApi` Gradle task
  - Uploads spec as GitHub Actions artifact
  - Artifact retained for 30 days
  - Publishes summary in GitHub job summary
- **Output:**
  - OpenAPI spec file available as workflow artifact: `openapi-spec`
  - Download from: Actions → Workflow run → Artifacts → `openapi-spec`

### Required Secrets

Add the following secrets to your repository (Settings → Secrets and variables → Actions):

**Repository-level secrets:**

- None required for basic CI/CD

**Environment secrets (in `BUILDS` environment):**

- `SONAR_TOKEN` - Your SonarCloud authentication token

To configure:

1. Go to Settings → Environments → Create environment → `BUILDS`
2. Add environment secret `SONAR_TOKEN` with your SonarCloud token
3. Optionally add deployment branch restrictions for added security

## Local Development

### Building Locally

```bash
# Build and test
make build
make test

# Generate documentation
make javadoc        # Generate Javadoc HTML docs (published to GitHub Pages)

# Run SonarQube analysis locally (requires SONAR_TOKEN in .env)
make sonar

# Build Docker image locally
make dbuild_local

# Push to GHCR or other registry
DOCKER_REGISTRY_IMAGE=ghcr.io/user/image \
DOCKER_TAG=latest \
DOCKER_USERNAME=username \
DOCKER_PASSWORD=token \
make dbuild_registry
```

### API Documentation

The application provides interactive Swagger UI at runtime. To view API documentation:

1. **Start the application:** `./gradlew bootRun`
2. **Access Swagger UI:** `http://localhost:8700/test/index.html`
3. **Fetch OpenAPI spec:** `http://localhost:8700/v3/api-docs` (JSON) or `http://localhost:8700/v3/api-docs?format=yaml` (YAML)

### Local Environment File

Create `.env` file in project root with:

```
SONAR_TOKEN=your_sonarcloud_token
```

## Setup Instructions

1. **SonarCloud Integration:**
   - Create or log in to https://sonarcloud.io
   - Create a new project for `spring-microservice-template`
   - Generate an authentication token
   - Create a `BUILDS` environment in GitHub with the `SONAR_TOKEN` secret

3. **Docker Registry (GHCR):**
   - No configuration needed - uses GitHub's built-in `GITHUB_TOKEN`
   - Ensure workflow has `packages: write` permission (already configured)
   - Images automatically available at `ghcr.io/jdellostritto/spring-microservice-template`

4. **GitHub Pages for Documentation:**
   - Enable GitHub Pages in repository Settings → Pages
   - Select "Deploy from a branch" 
   - Branch: `master`, Folder: `/ (root)` or `docs`
   - Pages will be published at: `https://yourusername.github.io/spring-microservice-template/`
   - Access API documentation at: `https://yourusername.github.io/spring-microservice-template/api.html`
   - Access Javadocs at: `https://yourusername.github.io/spring-microservice-template/javadoc/`

5. **Verify Workflows:**
   - Push a commit to `master` or create a PR
   - Navigate to Actions tab to monitor workflow execution
   - Check individual workflow runs for detailed output
   - Docker images will be available in GitHub Packages

## Troubleshooting

### SonarQube Analysis Fails

- Check that `SONAR_TOKEN` is configured in the `BUILDS` environment
- Verify token has permissions for the SonarCloud project
- Check that SonarCloud project key matches: `jdellostritto_spring-microservice-template`

### Docker Build Fails with Configuration Cache Error

- Configuration cache is disabled (`--no-configuration-cache`) for Jib tasks
- This is intentional due to Jib's incompatibility with Gradle configuration cache

### Test Results Not Appearing

- Verify tests are actually running: `make test`
- Check that test XML files exist in `build/test-results/test/`
- Test summary requires bash environment (works in GitHub Actions Linux runners)
