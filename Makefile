.PHONY: build test clean check stop bootrun dockerbuild image run down delete kube-apply kube-delete prune sonar test-report

# Load environment variables from .env file if it exists
-include .env

# OS Detection: Use gradlew.bat on Windows, ./gradlew on Unix/Linux/Mac
ifeq ($(OS),Windows_NT)
    GRADLEW := gradlew.bat
else
    GRADLEW := ./gradlew
endif

PROJECT ?= spring-microservice-template

IMAGE ?= jdellostritto/$(PROJECT)
BUILD ?= latest

COMPOSE ?= $(DOCKER_COMPOSE) $(RUN_CONFIG)
DOCKER_COMPOSE ?= docker-compose
RUN_CONFIG ?= -f docker-compose.yml
LOCAL_CONFIG ?= -f docker-compose.local.yml

build:
	$(GRADLEW) clean
	$(GRADLEW) build

test: build
	$(GRADLEW) test

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

clean:
	$(GRADLEW) clean

check:
	$(GRADLEW) check

stop:
	$(GRADLEW) --stop

bootrun:
	$(GRADLEW) bootRun

dockerbuild:
	$(GRADLEW) jibDockerBuild --no-configuration-cache

#Used with github actions CI
image:
	$(GRADLEW) jib -Djib.to.tags=${TAG} -Djib.to.auth.username=${DOCKER_USER} -Djib.to.auth.password=${DOCKER_ACCESS_TOKEN}

run:
	$(COMPOSE) $(LOCAL_CONFIG) up

down:
	$(COMPOSE) $(LOCAL_CONFIG) down
	$(COMPOSE) $(LOCAL_CONFIG) rm -f

delete:
	docker image rm $(IMAGE):$(BUILD)

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
