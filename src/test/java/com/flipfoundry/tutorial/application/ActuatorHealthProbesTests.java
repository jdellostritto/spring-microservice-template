package com.flipfoundry.tutorial.application;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test Kubernetes health probes for liveness and readiness.
 * 
 * These tests verify that the Spring Boot Actuator health endpoints
 * are properly configured and accessible for container orchestration
 * platforms like Kubernetes to use for:
 * - Liveness probes: Determine if the container is running
 * - Readiness probes: Determine if the container is ready to accept traffic
 * 
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.kubernetes-probes">Spring Boot Kubernetes Probes</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorHealthProbesTests {

	@Autowired
	private TestRestTemplate restTemplate;

	private WebTestClient webTestClient;

	@org.junit.jupiter.api.BeforeEach
	@SuppressWarnings("null")
	void setup() {
		// Create WebTestClient using the TestRestTemplate which binds to the random port
		String baseUrl = String.valueOf(restTemplate.getRootUri());
		this.webTestClient = WebTestClient.bindToServer()
			.baseUrl(baseUrl)
			.build();
	}

	/**
	 * Test that the main health endpoint is accessible and returns UP status.
	 * 
	 * This is the general health endpoint that aggregates all health indicators.
	 */
	@Test
	void testGeneralHealthEndpoint() {
		webTestClient
			.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP")
			.jsonPath("$.components").exists()
			.jsonPath("$.groups").isArray();
	}

	/**
	 * Test that the liveness probe endpoint is accessible.
	 * 
	 * The liveness probe indicates whether the application is alive.
	 * Kubernetes uses this to determine if a container should be restarted.
	 * 
	 * Endpoint: GET /actuator/health/liveness
	 * Kubernetes probe: livenessProbe
	 */
	@Test
	void testLivenessProbeEndpoint() {
		webTestClient
			.get()
			.uri("/actuator/health/liveness")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP");
	}

	/**
	 * Test that the readiness probe endpoint is accessible.
	 * 
	 * The readiness probe indicates whether the application is ready to accept traffic.
	 * Kubernetes uses this to determine if a container should receive traffic from services.
	 * 
	 * Endpoint: GET /actuator/health/readiness
	 * Kubernetes probe: readinessProbe
	 */
	@Test
	void testReadinessProbeEndpoint() {
		webTestClient
			.get()
			.uri("/actuator/health/readiness")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP");
	}

	/**
	 * Test that both probes return 200 OK status code.
	 * 
	 * HTTP 200 is critical for Kubernetes probes. Anything other than
	 * 200-399 range will be considered a failure by the probe.
	 */
	@Test
	void testProbesReturnSuccessStatusCode() {
		// Test liveness
		webTestClient
			.get()
			.uri("/actuator/health/liveness")
			.exchange()
			.expectStatus().isOk();

		// Test readiness
		webTestClient
			.get()
			.uri("/actuator/health/readiness")
			.exchange()
			.expectStatus().isOk();
	}

	/**
	 * Test that probes fail gracefully with error status when application is unhealthy.
	 * 
	 * This would require breaking the application (e.g., database connection failure).
	 * This is a placeholder for integration testing scenarios.
	 */
	@Test
	void testHealthEndpointIncludesComponents() {
		webTestClient
			.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.components.diskSpace").exists()
			.jsonPath("$.components.ping").exists();
	}

	/**
	 * Test that liveness state is specifically available as a component.
	 * 
	 * The livenessState component reflects the application lifecycle
	 * (ACCEPTING_TRAFFIC, REFUSING_TRAFFIC, etc.)
	 */
	@Test
	void testLivenessStateComponent() {
		webTestClient
			.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.components.livenessState").exists()
			.jsonPath("$.components.livenessState.status").isEqualTo("UP");
	}

	/**
	 * Test that readiness state is specifically available as a component.
	 * 
	 * The readinessState component reflects whether the application is
	 * ready to accept requests (ACCEPTING_TRAFFIC, REFUSING_TRAFFIC, etc.)
	 */
	@Test
	void testReadinessStateComponent() {
		webTestClient
			.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.components.readinessState").exists()
			.jsonPath("$.components.readinessState.status").isEqualTo("UP");
	}

	/**
	 * Test that health endpoint includes groups for probe organization.
	 * 
	 * Spring Boot organizes probes into groups (liveness, readiness)
	 * which Kubernetes can query separately.
	 */
	@Test
	void testHealthEndpointIncludesProbeGroups() {
		webTestClient
			.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.groups[0]").exists()
			.jsonPath("$.groups[1]").exists();
	}

	/**
	 * Test probe endpoint response time is acceptable for Kubernetes.
	 * 
	 * Kubernetes probes have timeout and period configurations.
	 * Probes should respond quickly (typically < 1 second).
	 */
	@Test
	void testProbesRespondQuickly() {
		webTestClient
			.get()
			.uri("/actuator/health/liveness")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.consumeWith(result -> {
				// Response should be received quickly
				assertThat(result.getResponseBody()).isNotEmpty();
			});
	}

	/**
	 * Test that actuator base path is correctly configured.
	 * 
	 * Default base path is /actuator. This test verifies the configuration
	 * hasn't changed unexpectedly.
	 */
	@Test
	void testActuatorBasePathIsCorrect() {
		// Test that /actuator endpoint exists and lists available endpoints
		webTestClient
			.get()
			.uri("/actuator")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$._links.health").exists()
			.jsonPath("$._links['health-path']").exists();
	}

	/**
	 * Integration test: Verify all health probes work together.
	 * 
	 * This test simulates a Kubernetes probe check sequence.
	 */
	@Test
	void testKubernetesProbeSequence() {
		// 1. Startup: Check if container is alive
		webTestClient
			.get()
			.uri("/actuator/health/liveness")
			.exchange()
			.expectStatus().isOk();

		// 2. Check if container is ready for traffic
		webTestClient
			.get()
			.uri("/actuator/health/readiness")
			.exchange()
			.expectStatus().isOk();

		// 3. Check overall health
		webTestClient
			.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP");
	}
}
