package com.flipfoundry.tutorial.application.dal;

import com.flipfoundry.tutorial.application.dal.entity.GreetingEntity;
import com.flipfoundry.tutorial.application.dal.repository.GreetingRepository;
import com.flipfoundry.tutorial.application.dal.service.GreetingPersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GreetingPersistenceServiceTest {

    @Mock
    private GreetingRepository repository;

    @InjectMocks
    private GreetingPersistenceService service;

    @Test
    void saveBuildsDomainEntityAndDelegates() {
        GreetingEntity saved = GreetingEntity.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .content("Hello, Alice!")
                .build();
        when(repository.save(any())).thenReturn(saved);

        GreetingEntity result = service.save("Alice", "Hello, Alice!");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Alice");

        ArgumentCaptor<GreetingEntity> captor = ArgumentCaptor.forClass(GreetingEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Alice");
        assertThat(captor.getValue().getContent()).isEqualTo("Hello, Alice!");
        assertThat(captor.getValue().getId()).isNull(); // id assigned by DB, not before save
    }

    @Test
    void findAllDelegatesAndReturnsList() {
        List<GreetingEntity> greetings = List.of(
                GreetingEntity.builder().name("Alice").content("Hello, Alice!").build(),
                GreetingEntity.builder().name("Bob").content("Hello, Bob!").build()
        );
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(greetings);

        List<GreetingEntity> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        verify(repository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void findAllReturnsEmptyListWhenRepositoryEmpty() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }
}
