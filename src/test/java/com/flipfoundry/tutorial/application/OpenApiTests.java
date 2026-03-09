package com.flipfoundry.tutorial.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenAPI endpoint tests for verifying OpenAPI documentation access.
 * Tests ensure that the API documentation endpoints are accessible and
 * return valid responses.
 *
 * @author <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiTests {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void openApiJsonShouldBeAccessible() {
		webTestClient.get()
			.uri("/v3/api-docs")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.value(body -> {
				assertThat(body).isNotEmpty();
				assertThat(body).contains("openapi", "info", "paths");
			});
	}

	@Test
	void openApiJsonShouldReturnValidJson() {
		webTestClient.get()
			.uri("/v3/api-docs")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.value(body -> {
				assertThat(body)
					.contains("\"openapi\"")
					.contains("\"info\"")
					.contains("\"title\"")
					.contains("\"version\"");
			});
	}

	@Test
	void openApiJsonShouldContainEndpoints() {
		webTestClient.get()
			.uri("/v3/api-docs")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.value(body -> assertThat(body).contains("paths"));
	}

	@Test
	void openApiJsonShouldContainApiInfo() {
		webTestClient.get()
			.uri("/v3/api-docs")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.value(body -> {
				assertThat(body)
					.contains("\"servers\"")
					.contains("\"components\"");
			});
	}
}

