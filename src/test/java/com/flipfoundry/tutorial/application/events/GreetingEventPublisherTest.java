package com.flipfoundry.tutorial.application.events;

import com.flipfoundry.tutorial.application.kafka.config.KafkaProducerProperties;
import com.flipfoundry.tutorial.application.kafka.producer.EventProducer;
import com.flipfoundry.tutorial.events.DepartingEvent;
import com.flipfoundry.tutorial.events.GreetingEvent;
import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GreetingEventPublisherTest {

    @Mock
    private EventProducer eventProducer;

    @Mock
    private KafkaProducerProperties kafkaProducerProperties;

    @InjectMocks
    private GreetingEventPublisher publisher;

    private static final RecordMetadata DUMMY_METADATA =
            new RecordMetadata(new TopicPartition("event-topic", 0), 0L, 0, 0L, 0, 0);

    // -----------------------------------------------------------------------
    // publishGreeting — producer enabled
    // -----------------------------------------------------------------------

    @Test
    void publishGreeting_withProducerEnabled_sendsGreetingEvent() {
        when(kafkaProducerProperties.getEventTopic()).thenReturn("event-topic");
        ArgumentCaptor<GreetingEvent> captor = ArgumentCaptor.forClass(GreetingEvent.class);
        when(eventProducer.send(eq("event-topic"), any(), captor.capture()))
                .thenReturn(Mono.just(DUMMY_METADATA));

        StepVerifier.create(publisher.publishGreeting("Alice", "Hello, Alice!", "en"))
                .verifyComplete();

        GreetingEvent sent = captor.getValue();
        assertThat(sent.getName()).isEqualTo("Alice");
        assertThat(sent.getMessage()).isEqualTo("Hello, Alice!");
        assertThat(sent.getLocale()).isEqualTo("en");
        assertThat(sent.getEventId()).isNotEmpty();
        assertThat(sent.getOccurredAt()).isNotNull();
    }

    @Test
    void publishGreeting_withProducerEnabled_usesConfiguredTopic() {
        when(kafkaProducerProperties.getEventTopic()).thenReturn("event-topic");
        when(eventProducer.send(eq("event-topic"), any(), any(GreetingEvent.class)))
                .thenReturn(Mono.just(DUMMY_METADATA));

        publisher.publishGreeting("Bob", "Hello, Bob!", "en").block();

        verify(eventProducer).send(eq("event-topic"), any(), any(GreetingEvent.class));
    }

    // -----------------------------------------------------------------------
    // publishGreeting — producer disabled (null)
    // -----------------------------------------------------------------------

    @Test
    void publishGreeting_withProducerDisabled_returnsEmptyMono() {
        // Create a publisher with no EventProducer injected (simulates disabled flag)
        GreetingEventPublisher disabledPublisher = new GreetingEventPublisher();

        StepVerifier.create(disabledPublisher.publishGreeting("Alice", "Hello, Alice!", "en"))
                .verifyComplete();

        verifyNoInteractions(eventProducer);
    }

    // -----------------------------------------------------------------------
    // publishDeparting — producer enabled
    // -----------------------------------------------------------------------

    @Test
    void publishDeparting_withProducerEnabled_sendsDepartingEvent() {
        when(kafkaProducerProperties.getEventTopic()).thenReturn("event-topic");
        ArgumentCaptor<DepartingEvent> captor = ArgumentCaptor.forClass(DepartingEvent.class);
        when(eventProducer.send(eq("event-topic"), any(), captor.capture()))
                .thenReturn(Mono.just(DUMMY_METADATA));

        StepVerifier.create(publisher.publishDeparting("Alice", "Goodbye!"))
                .verifyComplete();

        DepartingEvent sent = captor.getValue();
        assertThat(sent.getName()).isEqualTo("Alice");
        assertThat(sent.getFarewell()).isEqualTo("Goodbye!");
        assertThat(sent.getEventId()).isNotEmpty();
        assertThat(sent.getOccurredAt()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // publishDeparting — producer disabled (null)
    // -----------------------------------------------------------------------

    @Test
    void publishDeparting_withProducerDisabled_returnsEmptyMono() {
        GreetingEventPublisher disabledPublisher = new GreetingEventPublisher();

        StepVerifier.create(disabledPublisher.publishDeparting("Alice", "Goodbye!"))
                .verifyComplete();

        verifyNoInteractions(eventProducer);
    }
}
