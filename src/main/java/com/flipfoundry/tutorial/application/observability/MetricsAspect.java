package com.flipfoundry.tutorial.application.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for recording custom metrics on controller method execution.
 * 
 * This aspect intercepts all controller methods and automatically records:
 * - Request count
 * - Request duration
 * - Error count (if exception occurs)
 * 
 * This provides automatic observability without requiring changes to controller code.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsAspect {

    private final MetricsRegistry metricsRegistry;

    /**
     * Record metrics for greeting controller requests.
     * 
     * Pointcut matches all methods in GreetingController.
     */
    @Around("execution(* com.flipfoundry.tutorial.application.web.controller.GreetingController.*(..))")
    public Object recordGreetingMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        metricsRegistry.recordGreetingRequest();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            metricsRegistry.recordGreetingRequestDuration(duration, true);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            metricsRegistry.recordGreetingRequestDuration(duration, false);
            log.warn("Error in GreetingController", ex);
            throw ex;
        }
    }

    /**
     * Record metrics for departing controller requests.
     * 
     * Pointcut matches all methods in DepartingController.
     */
    @Around("execution(* com.flipfoundry.tutorial.application.web.controller.DepartingController.*(..))")
    public Object recordDepartingMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        metricsRegistry.recordDepartingRequest();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            metricsRegistry.recordDepartingRequestDuration(duration, true);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            metricsRegistry.recordDepartingRequestDuration(duration, false);
            log.warn("Error in DepartingController", ex);
            throw ex;
        }
    }
}
