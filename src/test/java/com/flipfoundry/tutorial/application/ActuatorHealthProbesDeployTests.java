package com.flipfoundry.tutorial.application;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test Kubernetes health probes for the DEPLOY profile.
 * 
 * This test class verifies that the deploy profile is properly secured
 * and only exposes the necessary endpoints while restricting debug/sensitive endpoints.
 * 
 * Deploy Profile Security Goals:
 * - Only expose: health, info, prometheus, livenessProbe, readinessProbe
 * - Disable: env, loggers, threaddump, heapdump, configprops, beans
 * - Set show-details to when-authorized to hide sensitive information
 * - Disable expensive health checks (diskSpace)
 * 
 * @see ActuatorHealthProbesTests for default profile tests
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("deploy")
class ActuatorHealthProbesDeployTests {

	@LocalManagementPort
	private int managementPort;

	private WebTestClient webTestClient;

	@BeforeEach
	void setup() {
		String baseUrl = "http://localhost:" + managementPort;
		this.webTestClient = WebTestClient.bindToServer()
			.baseUrl(baseUrl)
			.responseTimeout(Duration.ofSeconds(10))
			.build();
	}

	/**
	 * Test that liveness probe is available in deploy profile.
	 * 
	 * This endpoint is essential for Kubernetes and must be available.
	 */
	@Test
	void testLivenessProbeAvailableInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/health/liveness")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP");
	}

	/**
	 * Test that readiness probe is available in deploy profile.
	 * 
	 * This endpoint is essential for Kubernetes and must be available.
	 */
	@Test
	void testReadinessProbeAvailableInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/health/readiness")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP");
	}

	/**
	 * Test that health endpoint is available in deploy profile.
	 * 
	 * This is needed for general monitoring but should not expose
	 * sensitive details without authorization.
	 */
	@Test
	void testHealthEndpointAvailableInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP");
	}

	/**
	 * Test that info endpoint is available in deploy profile.
	 * 
	 * Info endpoint provides application metadata and is safe to expose.
	 */
	@Test
	void testInfoEndpointAvailable() {
		webTestClient.get()
			.uri("/actuator/info")
			.exchange()
			.expectStatus().value(status -> assertThat(status).isIn(200, 204));
	}

	/**
	 * Test that metrics endpoint is available in deploy profile.
	 * 
	 * Metrics endpoints are safe for monitoring/observability.
	 */
	@Test
	void testMetricsEndpointAvailable() {
		webTestClient.get()
			.uri("/actuator/metrics")
			.exchange()
			.expectStatus().isOk();
	}

	/**
	 * Test that env endpoint is NOT exposed in deploy profile.
	 * 
	 * The env endpoint exposes environment variables which may contain
	 * sensitive information like API keys, database credentials, etc.
	 * 
	 * Expected: 404 Not Found
	 */
	@Test
	void testEnvEndpointNotExposedInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/env")
			.exchange()
			.expectStatus().isNotFound();
	}

	/**
	 * Test that loggers endpoint is NOT exposed in deploy profile.
	 * 
	 * The loggers endpoint allows dynamic log level modification
	 * which could affect production system behavior.
	 * 
	 * Expected: 404 Not Found
	 */
	@Test
	void testLoggersEndpointNotExposedInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/loggers")
			.exchange()
			.expectStatus().isNotFound();
	}

	/**
	 * Test that threaddump endpoint is NOT exposed in deploy profile.
	 * 
	 * Thread dumps expose internal application state and threading information
	 * that could reveal security or architectural details.
	 * 
	 * Expected: 404 Not Found
	 */
	@Test
	void testThreaddumpEndpointNotExposedInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/threaddump")
			.exchange()
			.expectStatus().isNotFound();
	}

	/**
	 * Test that heapdump endpoint is NOT exposed in deploy profile.
	 * 
	 * Heap dumps contain all memory state including potentially sensitive data.
	 * They're large and resource-intensive, unsuitable for production exposure.
	 * 
	 * Expected: 404 Not Found
	 */
	@Test
	void testHeapdumpEndpointNotExposedInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/heapdump")
			.exchange()
			.expectStatus().isNotFound();
	}

	/**
	 * Test that configprops endpoint is NOT exposed in deploy profile.
	 * 
	 * Config properties may contain sensitive settings, database URLs,
	 * API endpoints, and other infrastructure details.
	 * 
	 * Expected: 404 Not Found
	 */
	@Test
	void testConfigpropsEndpointNotExposedInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/configprops")
			.exchange()
			.expectStatus().isNotFound();
	}

	/**
	 * Test that beans endpoint is NOT exposed in deploy profile.
	 * 
	 * The beans endpoint reveals the entire Spring bean structure
	 * which could expose implementation details and architectural patterns.
	 * 
	 * Expected: 404 Not Found
	 */
	@Test
	void testBeansEndpointNotExposedInDeployProfile() {
		webTestClient.get()
			.uri("/actuator/beans")
			.exchange()
			.expectStatus().isNotFound();
	}

	/**
	 * Test that health details are restricted without authorization.
	 * 
	 * In deploy profile, show-details is set to "when-authorized"
	 * which means the response should not include component details.
	 */
	@Test
	void testHealthDetailsRestricted() {
		webTestClient.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP")
			.jsonPath("$.components").doesNotExist();
	}

	/**
	 * Test that actuator base path is correct in deploy profile.
	 * 
	 * The /actuator endpoint should list available endpoints
	 * (showing only the ones exposed in deploy profile).
	 */
	@Test
	void testActuatorBasePathLimitedInDeployProfile() {
		webTestClient.get()
			.uri("/actuator")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.consumeWith(result -> {
				String body = result.getResponseBody();
				assertThat(body).contains("health", "info", "metrics");
				assertThat(body).doesNotContain("env", "loggers", "beans");
			});
	}

	/**
	 * Integration test: Verify probe availability and security restrictions.
	 * 
	 * This test simulates a production deployment scenario:
	 * 1. Kubernetes can check liveness/readiness probes
	 * 2. Sensitive endpoints are not accessible
	 * 3. Health details are restricted
	 */
	@Test
	void testDeployProfileSecurityModel() {
		// 1. Probes must work for Kubernetes
		webTestClient.get()
			.uri("/actuator/health/liveness")
			.exchange()
			.expectStatus().isOk();

		webTestClient.get()
			.uri("/actuator/health/readiness")
			.exchange()
			.expectStatus().isOk();

		// 2. Sensitive endpoints must be blocked
		webTestClient.get()
			.uri("/actuator/env")
			.exchange()
			.expectStatus().isNotFound();

		webTestClient.get()
			.uri("/actuator/loggers")
			.exchange()
			.expectStatus().isNotFound();

		// 3. Safe monitoring endpoints must be available
		webTestClient.get()
			.uri("/actuator/metrics")
			.exchange()
			.expectStatus().isOk();

		// 4. Health details must be restricted
		webTestClient.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP")
			.jsonPath("$.components").doesNotExist();
	}
}
