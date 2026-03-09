package com.flipfoundry.tutorial.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class DepartingWebApplicationTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	void departEndpointV1ShouldReturnContentAndDate() {
		this.webClient.get().uri("/flip/departing/depart")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.departing.v1+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.content").exists()
				.jsonPath("$.content").isEqualTo("Goodbye")
				.jsonPath("$.date").exists();
	}

	@Test
	void departEndpointV1ShouldReturn406WithBadAccept() {
		this.webClient.get().uri("/flip/departing/depart")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.foo.v1+json"))
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@Test
	void departEndpointShouldHandleExceptionGracefully() {
		// Test that exception handling doesn't break the endpoint - multiple consecutive requests should all succeed
		for (int i = 0; i < 3; i++) {
			this.webClient.get().uri("/flip/departing/depart")
					.accept(MediaType.valueOf("application/vnd.flipfoundry.departing.v1+json"))
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.content").isEqualTo("Goodbye")
					.jsonPath("$.date").exists();
		}
	}
}
