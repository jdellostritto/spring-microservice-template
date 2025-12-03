#!/bin/sh
export REPO=opengovorg
export ENVIRONMENT=deploy
export BUILD=latest
export REPCOUNT=1
export PROJECT=java-service-simple
export KEYCLOAK_URI=http://keycloak-headless:8080
export OTEL_EXPORTER_OTLP_ENDPOINT=http://traces.grafana.svc.cluster.local:4317
