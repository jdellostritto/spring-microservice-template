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

### 3. **docker.yml** - Docker Build and Push
- **Trigger:** Push to `master` branch (manual dispatch available)
- **Actions:**
  - Build Docker image using Jib
  - Login to Docker Hub
  - Push to Docker Hub with `latest` and SHA tags
  - Requires GitHub Secrets:
    - `DOCKERHUB_USERNAME` - Your Docker Hub username
    - `DOCKERHUB_TOKEN` - Your Docker Hub access token

## GitHub Secrets to Configure

Add the following secrets to your repository (Settings → Secrets and variables → Actions):

```
SONAR_TOKEN=your_sonarqube_token_here
SONAR_HOST_URL=http://localhost:9000  (if not using SonarCloud)
DOCKERHUB_USERNAME=jdellostritto
DOCKERHUB_TOKEN=your_dockerhub_token_here
```

## Next Steps

1. **SonarQube Integration:**
   - Obtain your SonarQube token from your SonarQube instance
   - Add `SONAR_TOKEN` to GitHub Secrets

2. **Docker Hub Integration (Optional):**
   - Generate a Docker Hub access token
   - Add `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` to GitHub Secrets

3. **Verify Workflows:**
   - Push a commit to `master` or create a PR
   - Navigate to Actions tab to monitor workflow execution
