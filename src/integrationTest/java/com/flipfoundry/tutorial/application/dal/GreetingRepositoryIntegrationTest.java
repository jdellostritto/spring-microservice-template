package com.flipfoundry.tutorial.application.dal;

import com.flipfoundry.tutorial.application.dal.entity.GreetingEntity;
import com.flipfoundry.tutorial.application.dal.repository.GreetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Data-layer integration test — runs the repository against a real PostgreSQL instance
 * spun up by Testcontainers. Verifies behaviour that H2 cannot faithfully reproduce
 * (UUID primary key generation, CreationTimestamp precision, ordering guarantee).
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GreetingRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private GreetingRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void savePersistsEntityAndAssignsUuid() {
        GreetingEntity entity = GreetingEntity.builder()
                .name("Alice")
                .content("Hello, Alice!")
                .build();

        GreetingEntity saved = repository.saveAndFlush(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alice");
        assertThat(saved.getContent()).isEqualTo("Hello, Alice!");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findAllByOrderByCreatedAtDescReturnsNewestFirst() throws InterruptedException {
        repository.save(GreetingEntity.builder().name("Alice").content("Hello, Alice!").build());
        Thread.sleep(10); // ensure distinct DB-assigned timestamps
        repository.save(GreetingEntity.builder().name("Bob").content("Hello, Bob!").build());

        List<GreetingEntity> results = repository.findAllByOrderByCreatedAtDesc();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Bob");   // newest first
        assertThat(results.get(1).getName()).isEqualTo("Alice");
    }

    @Test
    void findAllReturnsEmptyWhenNothingSaved() {
        assertThat(repository.findAllByOrderByCreatedAtDesc()).isEmpty();
    }

    @Test
    void saveMultipleAndCountAll() {
        repository.save(GreetingEntity.builder().name("Alice").content("Hello, Alice!").build());
        repository.save(GreetingEntity.builder().name("Bob").content("Hello, Bob!").build());
        repository.save(GreetingEntity.builder().name("Carol").content("Hello, Carol!").build());

        assertThat(repository.count()).isEqualTo(3);
    }
}
