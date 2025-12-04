package com.flipfoundry.tutorial.application.observability;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Configuration for application observability.
 * 
 * This component initializes and configures observability features including:
 * - Custom application metrics via Micrometer
 * - OpenTelemetry instrumentation
 * - Distributed tracing
 */
@Component
@RequiredArgsConstructor
public class ObservabilityConfiguration {

    private final MetricsRegistry metricsRegistry;

    /**
     * Initialize observability components after application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeObservability() {
        metricsRegistry.initializeMetrics();
    }
}
