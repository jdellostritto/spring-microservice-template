package com.flipfoundry.tutorial.application.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsRegistryTest {

    private MeterRegistry meterRegistry;
    private MetricsRegistry metricsRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsRegistry = new MetricsRegistry(meterRegistry);
        metricsRegistry.initializeMetrics();
    }

    // -----------------------------------------------------------------------
    // Greeting metrics
    // -----------------------------------------------------------------------

    @Test
    void recordGreetingRequest_incrementsCounter() {
        metricsRegistry.recordGreetingRequest();
        metricsRegistry.recordGreetingRequest();

        Counter counter = meterRegistry.find("greeting.requests.total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    void recordGreetingRequestDuration_successDoesNotIncrementErrorCounter() {
        metricsRegistry.recordGreetingRequestDuration(50, true);

        Counter errors = meterRegistry.find("greeting.errors.total").counter();
        assertThat(errors).isNotNull();
        assertThat(errors.count()).isEqualTo(0.0);
    }

    @Test
    void recordGreetingRequestDuration_failureIncrementsErrorCounter() {
        metricsRegistry.recordGreetingRequestDuration(50, false);

        Counter errors = meterRegistry.find("greeting.errors.total").counter();
        assertThat(errors).isNotNull();
        assertThat(errors.count()).isEqualTo(1.0);
    }

    @Test
    void recordGreetingError_incrementsErrorCounter() {
        metricsRegistry.recordGreetingError();
        metricsRegistry.recordGreetingError();

        Counter errors = meterRegistry.find("greeting.errors.total").counter();
        assertThat(errors).isNotNull();
        assertThat(errors.count()).isEqualTo(2.0);
    }

    // -----------------------------------------------------------------------
    // Departing metrics
    // -----------------------------------------------------------------------

    @Test
    void recordDepartingRequest_incrementsCounter() {
        metricsRegistry.recordDepartingRequest();

        Counter counter = meterRegistry.find("departing.requests.total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordDepartingRequestDuration_successDoesNotIncrementErrorCounter() {
        metricsRegistry.recordDepartingRequestDuration(30, true);

        Counter errors = meterRegistry.find("departing.errors.total").counter();
        assertThat(errors).isNotNull();
        assertThat(errors.count()).isEqualTo(0.0);
    }

    @Test
    void recordDepartingRequestDuration_failureIncrementsErrorCounter() {
        metricsRegistry.recordDepartingRequestDuration(30, false);

        Counter errors = meterRegistry.find("departing.errors.total").counter();
        assertThat(errors).isNotNull();
        assertThat(errors.count()).isEqualTo(1.0);
    }

    @Test
    void recordDepartingError_incrementsErrorCounter() {
        metricsRegistry.recordDepartingError();

        Counter errors = meterRegistry.find("departing.errors.total").counter();
        assertThat(errors).isNotNull();
        assertThat(errors.count()).isEqualTo(1.0);
    }
}
