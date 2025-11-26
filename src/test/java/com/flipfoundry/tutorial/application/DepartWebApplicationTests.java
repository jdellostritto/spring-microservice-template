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
public class DepartWebApplicationTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	public void departEndpointV1ShouldReturnContentAndDate() throws Exception {
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
	public void departEndpointV1ShouldReturn406WithBadAccept() throws Exception {
		this.webClient.get().uri("/flip/departing/depart")
				.accept(MediaType.valueOf("application/vnd.flipfoundry.foo.v1+json"))
				.exchange()
				.expectStatus().is4xxClientError();
	}


}
