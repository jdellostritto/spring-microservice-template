package com.flipfoundry.tutorial.application.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Registry for custom application metrics.
 * 
 * This component provides a centralized place to define and manage custom metrics
 * for the Spring Microservice Template application. All metrics are automatically
 * exposed via the /actuator/prometheus endpoint and collected by OpenTelemetry.
 * 
 * Metrics are categorized by feature/domain:
 * - greeting.* - Greeting controller metrics
 * - departing.* - Departing controller metrics
 * 
 * @see <a href="https://micrometer.io/">Micrometer Documentation</a>
 */
@Component
@RequiredArgsConstructor
public class MetricsRegistry {

    private final MeterRegistry meterRegistry;

    // =====================================================
    // Greeting Controller Metrics
    // =====================================================

    private Counter greetingRequests;
    private Timer greetingRequestDuration;
    private Counter greetingErrors;

    // =====================================================
    // Departing Controller Metrics
    // =====================================================

    private Counter departingRequests;
    private Timer departingRequestDuration;
    private Counter departingErrors;

    /**
     * Initialize all custom metrics.
     * This method is called automatically after bean construction.
     */
    public void initializeMetrics() {
        initializeGreetingMetrics();
        initializeDepartingMetrics();
    }

    // =====================================================
    // Greeting Metrics Methods
    // =====================================================

    /**
     * Initialize greeting controller metrics.
     */
    private void initializeGreetingMetrics() {
        greetingRequests = Counter.builder("greeting.requests.total")
            .description("Total number of greeting requests")
            .tag("controller", "greeting")
            .register(meterRegistry);

        greetingRequestDuration = Timer.builder("greeting.requests.duration")
            .description("Duration of greeting requests")
            .tag("controller", "greeting")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        greetingErrors = Counter.builder("greeting.errors.total")
            .description("Total number of greeting errors")
            .tag("controller", "greeting")
            .register(meterRegistry);
    }

    /**
     * Record a greeting request.
     * Increments the request counter.
     */
    public void recordGreetingRequest() {
        greetingRequests.increment();
    }

    /**
     * Record greeting request duration and result.
     * 
     * @param durationMs Duration in milliseconds
     * @param success Whether the request was successful
     */
    public void recordGreetingRequestDuration(long durationMs, boolean success) {
        greetingRequestDuration.record(java.time.Duration.ofMillis(durationMs));
        if (!success) {
            greetingErrors.increment();
        }
    }

    /**
     * Record a greeting error.
     * Increments the error counter.
     */
    public void recordGreetingError() {
        greetingErrors.increment();
    }

    // =====================================================
    // Departing Metrics Methods
    // =====================================================

    /**
     * Initialize departing controller metrics.
     */
    private void initializeDepartingMetrics() {
        departingRequests = Counter.builder("departing.requests.total")
            .description("Total number of departing requests")
            .tag("controller", "departing")
            .register(meterRegistry);

        departingRequestDuration = Timer.builder("departing.requests.duration")
            .description("Duration of departing requests")
            .tag("controller", "departing")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        departingErrors = Counter.builder("departing.errors.total")
            .description("Total number of departing errors")
            .tag("controller", "departing")
            .register(meterRegistry);
    }

    /**
     * Record a departing request.
     * Increments the request counter.
     */
    public void recordDepartingRequest() {
        departingRequests.increment();
    }

    /**
     * Record departing request duration and result.
     * 
     * @param durationMs Duration in milliseconds
     * @param success Whether the request was successful
     */
    public void recordDepartingRequestDuration(long durationMs, boolean success) {
        departingRequestDuration.record(java.time.Duration.ofMillis(durationMs));
        if (!success) {
            departingErrors.increment();
        }
    }

    /**
     * Record a departing error.
     * Increments the error counter.
     */
    public void recordDepartingError() {
        departingErrors.increment();
    }
}
