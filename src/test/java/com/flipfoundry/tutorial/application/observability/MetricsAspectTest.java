package com.flipfoundry.tutorial.application.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    private MeterRegistry meterRegistry;
    private MetricsRegistry metricsRegistry;
    private MetricsAspect aspect;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsRegistry = new MetricsRegistry(meterRegistry);
        metricsRegistry.initializeMetrics();
        aspect = new MetricsAspect(metricsRegistry);
    }

    // -----------------------------------------------------------------------
    // Greeting aspect — success path
    // -----------------------------------------------------------------------

    @Test
    void recordGreetingMetrics_successPath_returnsResultAndIncrementsRequest() throws Throwable {
        Object expectedResult = new Object();
        when(joinPoint.proceed()).thenReturn(expectedResult);

        Object result = aspect.recordGreetingMetrics(joinPoint);

        assertThat(result).isSameAs(expectedResult);
        verify(joinPoint).proceed();
        assertThat(counterValue("greeting.requests.total")).isEqualTo(1.0);
        assertThat(counterValue("greeting.errors.total")).isEqualTo(0.0);
    }

    @Test
    void recordGreetingMetrics_exceptionPath_rethrowsAndRecordsFailure() throws Throwable {
        RuntimeException thrown = new RuntimeException("boom");
        when(joinPoint.proceed()).thenThrow(thrown);

        assertThatThrownBy(() -> aspect.recordGreetingMetrics(joinPoint))
                .isSameAs(thrown);

        assertThat(counterValue("greeting.errors.total")).isEqualTo(1.0);
    }

    // -----------------------------------------------------------------------
    // Departing aspect — success path
    // -----------------------------------------------------------------------

    @Test
    void recordDepartingMetrics_successPath_incrementsRequestCounter() throws Throwable {
        when(joinPoint.proceed()).thenReturn("ok");

        aspect.recordDepartingMetrics(joinPoint);

        assertThat(counterValue("departing.requests.total")).isEqualTo(1.0);
        assertThat(counterValue("departing.errors.total")).isEqualTo(0.0);
    }

    @Test
    void recordDepartingMetrics_exceptionPath_rethrowsAndRecordsFailure() throws Throwable {
        RuntimeException thrown = new RuntimeException("boom");
        when(joinPoint.proceed()).thenThrow(thrown);

        assertThatThrownBy(() -> aspect.recordDepartingMetrics(joinPoint))
                .isSameAs(thrown);

        assertThat(counterValue("departing.errors.total")).isEqualTo(1.0);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private double counterValue(String name) {
        var counter = meterRegistry.find(name).counter();
        return counter != null ? counter.count() : 0.0;
    }
}
