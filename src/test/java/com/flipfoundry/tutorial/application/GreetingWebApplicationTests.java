package com.flipfoundry.tutorial.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class GreetingWebApplicationTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	void greetEndpointV1ShouldReturnId() {
		this.webClient.get().uri("/flip/greeting/greet")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v1+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").exists();
	}

	@Test
	void greetEndpointV2ShouldNotReturnId() {
		this.webClient.get().uri("/flip/greeting/greet")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v2+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").doesNotExist();
	}

	@Test
	void greetEndpointV2ShouldReturnContent() {
		this.webClient.get().uri("/flip/greeting/greet?name=World")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v2+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.content").isEqualTo("Hello, World!");
	}

	@Test
	void historyEndpointShouldReturnOkAndArray() {
		this.webClient.get().uri("/flip/greeting/history")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v2+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$").isArray();
	}

	@Test
	void historyEndpointShouldContainGreetingAfterGreetCall() throws InterruptedException {
		// Trigger a greeting — the async save is fire-and-forget on boundedElastic
		this.webClient.get().uri("/flip/greeting/greet?name=HistoryTest")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v2+json"))
				.exchange()
				.expectStatus().isOk();

		// Give the boundedElastic thread time to complete the save
		Thread.sleep(2000);

		this.webClient.get().uri("/flip/greeting/history")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v2+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(com.flipfoundry.tutorial.application.web.dto.GreetingDTOV2.class)
				.value(list -> assertThat(list)
						.isNotEmpty()
						.anyMatch(dto -> "Hello, HistoryTest!".equals(dto.getContent())));
	}

	@Test
	void departEndpointShouldReturnAnEmptyDate() {
		this.webClient.get().uri("/flip/greeting/depart")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.greeting.v1+json"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.date").isEmpty()
				.jsonPath("$.content").isEqualTo("Goodbye");
	}

	@Test
	void departEndpointV1ShouldReturn406WithBadAccept() {
		this.webClient.get().uri("/flip/greeting/greet")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.foo.v1+json"))
				.exchange()
				.expectStatus().is4xxClientError();
	}
}

