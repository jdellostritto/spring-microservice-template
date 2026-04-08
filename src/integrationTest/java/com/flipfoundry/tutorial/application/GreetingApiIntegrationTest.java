package com.flipfoundry.tutorial.application;

import com.flipfoundry.tutorial.application.dal.repository.GreetingRepository;
import com.flipfoundry.tutorial.application.web.dto.GreetingDTOV2;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test: HTTP layer → service → JPA → real PostgreSQL.
 * Kafka and OTel are disabled via src/integrationTest/resources/application.yml.
 *
 * The greet endpoint persists asynchronously (fire-and-forget), so the history
 * assertions use Awaitility to poll rather than assuming the write is instant.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class GreetingApiIntegrationTest {

    private static final MediaType GREETING_V2 =
            MediaType.parseMediaType("application/vnd.flipfoundry.greeting.v2+json");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GreetingRepository greetingRepository;

    @BeforeEach
    void cleanDatabase() {
        greetingRepository.deleteAll();
    }

    @Test
    void greetReturnsCorrectMessage() {
        webTestClient.get()
                .uri("/flip/greeting/greet?name=Alice")
                .accept(GREETING_V2)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GreetingDTOV2.class)
                .value(dto -> assertThat(dto.getContent()).isEqualTo("Hello, Alice!"));
    }

    @Test
    void greetDefaultsToWorld() {
        webTestClient.get()
                .uri("/flip/greeting/greet")
                .accept(GREETING_V2)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GreetingDTOV2.class)
                .value(dto -> assertThat(dto.getContent()).isEqualTo("Hello, World!"));
    }

    @Test
    void historyReturnsEmptyBeforeAnyGreeting() {
        webTestClient.get()
                .uri("/flip/greeting/history")
                .accept(GREETING_V2)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GreetingDTOV2.class)
                .hasSize(0);
    }

    @Test
    void greetPersistsAndAppearsInHistory() {
        webTestClient.get()
                .uri("/flip/greeting/greet?name=Bob")
                .accept(GREETING_V2)
                .exchange()
                .expectStatus().isOk();

        // The persist is fire-and-forget on a bounded-elastic thread; poll until it lands.
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() ->
                        webTestClient.get()
                                .uri("/flip/greeting/history")
                                .accept(GREETING_V2)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBodyList(GreetingDTOV2.class)
                                .value(list -> assertThat(list)
                                        .extracting(GreetingDTOV2::getContent)
                                        .contains("Hello, Bob!"))
                );
    }
}
