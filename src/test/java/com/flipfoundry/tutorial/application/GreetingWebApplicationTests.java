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
public class GreetingWebApplicationTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	public void greetEndpointV1ShouldReturnId() throws Exception {
		this.webClient.get().uri("/flip/greeting/greet")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v1+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").exists();
	}
	@Test
	public void greetEndpointV2ShouldNotReturnId() throws Exception {
		this.webClient.get().uri("/flip/greeting/greet")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v2+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").doesNotExist();
	}

	@Test
	public void departEndpointShouldReturnAnEmptyDate() throws Exception {
		this.webClient.get().uri("/flip/greeting/depart")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v1+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.date").isEmpty()
				.jsonPath("$.content").isEqualTo("Goodbye");


	}

	@Test
	public void departEndpointV1ShouldReturn406WithBadAccept() throws Exception {
		this.webClient.get().uri("/flip/greeting/greet")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.foo.v1+json"))
				.exchange()
				.expectStatus().is4xxClientError();
	}
}
