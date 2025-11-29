# GitHub Actions Setup for spring-microservice-template

## Workflows Configured

### 1. **build.yml** - Build and Test
- **Trigger:** Push to `master`/`develop`, Pull Requests
- **Actions:**
  - Compile code with Java 21
  - Run all tests (unit + integration)
  - Generate JaCoCo coverage reports
  - Upload test and coverage reports as artifacts
  - Publish test results as check summary

### 2. **sonar.yml** - SonarQube Analysis
- **Trigger:** Push to `master`/`develop`, Pull Requests
- **Actions:**
  - Build project and run SonarQube analysis
  - Requires GitHub Secrets:
    - `SONAR_TOKEN` - Your SonarQube authentication token
    - `SONAR_HOST_URL` - Your SonarQube server URL (optional)
  - Uploads build reports as artifacts

### 3. **docker.yml** - Docker Build and Push to GHCR
- **Trigger:** Push to `master` branch (manual dispatch available)
- **Actions:**
  - Build Docker image using Docker build-push-action
  - Push to GitHub Container Registry (GHCR)
  - Tags: `latest`, `master`, and `sha-<commit-hash>`
  - **No secrets required** - uses built-in `GITHUB_TOKEN`
- **Image URL:** `ghcr.io/jdellostritto/spring-microservice-template:latest`

## GitHub Secrets to Configure

Add the following secrets to your repository (Settings → Secrets and variables → Actions):

```
SONAR_TOKEN=your_sonarqube_token_here
SONAR_HOST_URL=http://localhost:9000  (if not using SonarCloud)
```

**Docker Registry:** No secrets required for GHCR - uses built-in `GITHUB_TOKEN`

## Next Steps

1. **SonarQube Integration:**
   - Obtain your SonarQube token from your SonarQube instance
   - Add `SONAR_TOKEN` to GitHub Secrets

2. **Docker Images:**
   - Images are automatically built and pushed to GHCR
   - No configuration needed - uses GitHub's built-in token
   - Access images at: `ghcr.io/jdellostritto/spring-microservice-template:latest`

3. **Verify Workflows:**
   - Push a commit to `master` or create a PR
   - Navigate to Actions tab to monitor workflow execution
   - Docker images will be available in your GitHub Packages
