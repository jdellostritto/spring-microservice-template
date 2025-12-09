.PHONY: build test clean check stop bootrun dbuild_local dbuild_registry javadoc openapi run down delete kube-apply kube-delete prune sonar test-report

# Load environment variables from .env file if it exists
-include .env

# OS Detection: Use gradlew.bat on Windows, ./gradlew on Unix/Linux/Mac
ifeq ($(OS),Windows_NT)
    GRADLEW := gradlew.bat
else
    GRADLEW := ./gradlew
endif

PROJECT ?= spring-microservice-template

#IMAGE ?= /$(PROJECT)
BUILD ?= latest

# Docker registry configuration (can be overridden by GitHub Actions or environment)
# Examples: ghcr.io/jdellostritto/spring-microservice-template, jdellostritto/spring-microservice-template, etc.
DOCKER_REGISTRY ?= ghcr.io/jdellostritto
DOCKER_REGISTRY_IMAGE ?= $(DOCKER_REGISTRY)/$(PROJECT)
DOCKER_TAG ?= $(BUILD)
DOCKER_USERNAME ?= $(DOCKER_USER)
DOCKER_PASSWORD ?= $(DOCKER_ACCESS_TOKEN)

COMPOSE ?= $(DOCKER_COMPOSE) $(RUN_CONFIG)
DOCKER_COMPOSE ?= docker-compose
RUN_CONFIG ?= -f docker-compose.yml
LOCAL_CONFIG ?= -f docker-compose.local.yml

build:
	$(GRADLEW) clean
	$(GRADLEW) build

test: build
	$(GRADLEW) test

clean:
	$(GRADLEW) clean

check:
	$(GRADLEW) check

stop:
	$(GRADLEW) --stop

# Generate Javadoc documentation
javadoc:
	$(GRADLEW) javadoc

# Generate OpenAPI specification
openapi: build
	$(GRADLEW) generateOpenApiDocs --no-configuration-cache

bootrun:
	$(GRADLEW) bootRun

# Build Docker image locally to Docker daemon
dbuild-local:
	$(GRADLEW) jibDockerBuild -Djib.to.image=$(PROJECT) -Djib.to.tags=local --no-configuration-cache

# Generic target to build and push Docker image to any registry
# Usage: make docker-push DOCKER_REGISTRY_IMAGE=ghcr.io/user/image DOCKER_TAG=latest DOCKER_USERNAME=user DOCKER_PASSWORD=token
# Make sure you set environment variables and call: make dbuild_registry
dbuild-registry:
	$(GRADLEW) jib --no-configuration-cache \
		-Djib.to.image=$(DOCKER_REGISTRY_IMAGE) \
		-Djib.to.tags=$(DOCKER_TAG) \
		-Djib.to.auth.username=$(DOCKER_USERNAME) \
		-Djib.to.auth.password=$(DOCKER_PASSWORD)

up:
	$(COMPOSE) $(LOCAL_CONFIG) up -d

down:
	$(COMPOSE) $(LOCAL_CONFIG) down
	$(COMPOSE) $(LOCAL_CONFIG) rm -f

up-obs:
	cd observability-stack && $(DOCKER_COMPOSE) $(RUN_CONFIG) up -d

down-obs:
	cd observability-stack && $(DOCKER_COMPOSE) $(RUN_CONFIG) down

delete:
	docker image rm $(DOCKER_REGISTRY_IMAGE):$(DOCKER_TAG)

# Run Static code analysis
sonar:
	@if [ -z "$(SONAR_TOKEN)" ]; then \
		echo "⚠️  SONAR_TOKEN not set. Skipping SonarQube analysis."; \
		$(GRADLEW) build --no-configuration-cache; \
	else \
		$(GRADLEW) build sonar --no-configuration-cache -Dsonar.token=$(SONAR_TOKEN); \
	fi


# KUBERNETES *NIX/BASH RUN.
# . Run command below first for minikube and make sure the image is available.
#   eval $(minikube -p minikube docker-env)
kube-apply:
	envsubst < ./kubernetes/kubernetes.chart > ./kubernetes/$(PROJECT).yml
	cat ./kubernetes/kubernetes.chart
	kubectl apply -f ./kubernetes/$(PROJECT).yml

kube-delete:
	kubectl delete -f ./kubernetes/$(PROJECT).yml
	rm ./kubernetes/$(PROJECT).yml

# PRUNE
prune:
	docker system prune -f
	docker network prune -f
	docker volume prune -f

# Used be GitHub Actions to append test results to the summary.
test-report:
	@echo "## 📊 Test Results" >> $(GITHUB_STEP_SUMMARY)
	@echo "" >> $(GITHUB_STEP_SUMMARY)
	@if [ -d "build/test-results/test" ] && [ "$$(ls -A build/test-results/test/*.xml 2>/dev/null | wc -l)" -gt 0 ]; then \
		echo "✅ Tests completed successfully" >> $(GITHUB_STEP_SUMMARY); \
		echo "" >> $(GITHUB_STEP_SUMMARY); \
		echo "📈 Test reports available in artifacts:" >> $(GITHUB_STEP_SUMMARY); \
		echo "- JaCoCo Coverage Report" >> $(GITHUB_STEP_SUMMARY); \
		echo "- Detailed Test Report" >> $(GITHUB_STEP_SUMMARY); \
	else \
		echo "⚠️ No test results found" >> $(GITHUB_STEP_SUMMARY); \
	fi