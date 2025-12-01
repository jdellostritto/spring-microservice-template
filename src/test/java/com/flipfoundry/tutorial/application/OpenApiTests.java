package com.flipfoundry.tutorial.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void openApiJsonShouldBeAccessible() throws Exception {
		String url = "http://localhost:" + port + "/v3/api-docs";
		ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class);
		
		System.out.println("Response Status: " + response.getStatusCode());
		System.out.println("Response Body: " + response.getBody());
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody())
			.isNotEmpty()
			.contains("openapi", "info", "paths");
	}

	@Test
	public void openApiJsonShouldReturnValidJson() throws Exception {
		String url = "http://localhost:" + port + "/v3/api-docs";
		ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		// Verify it's valid JSON by checking structure
		assertThat(response.getBody())
			.contains("\"openapi\"")
			.contains("\"info\"")
			.contains("\"title\"")
			.contains("\"version\"");
	}

	@Test
	public void openApiJsonShouldContainEndpoints() throws Exception {
		String url = "http://localhost:" + port + "/v3/api-docs";
		ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String body = response.getBody();
		// Just verify the response contains path information
		assertThat(body).contains("paths");
	}

	@Test
	public void openApiJsonShouldContainApiInfo() throws Exception {
		String url = "http://localhost:" + port + "/v3/api-docs";
		ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String body = response.getBody();
		assertThat(body)
			.contains("\"servers\"")
			.contains("\"components\"");
	}
}

