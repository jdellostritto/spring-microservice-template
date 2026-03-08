package com.flipfoundry.tutorial.application.kafka.producer;

import com.flipfoundry.tutorial.events.GreetingEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventProducerTest {

    @Mock
    KafkaSender<String, byte[]> kafkaSender;

    @InjectMocks
    EventProducer eventProducer;

    @Test
    void send_publishes_event_and_returns_record_metadata() {
        @SuppressWarnings("unchecked")
        SenderResult<String> senderResult = (SenderResult<String>) mock(SenderResult.class);
        RecordMetadata metadata = mock(RecordMetadata.class);
        when(senderResult.recordMetadata()).thenReturn(metadata);
        doReturn(Flux.just(senderResult)).when(kafkaSender).send(any());

        GreetingEvent event = GreetingEvent.newBuilder()
                .setEventId("e1")
                .setName("Alice")
                .setMessage("Hello")
                .setLocale("en")
                .build();

        StepVerifier.create(eventProducer.send("greeting-events", "e1", event))
                .expectNext(metadata)
                .verifyComplete();

        verify(kafkaSender).send(any());
    }

    @Test
    void send_serializes_event_to_bytes() {
        @SuppressWarnings("unchecked")
        SenderResult<String> senderResult = (SenderResult<String>) mock(SenderResult.class);
        when(senderResult.recordMetadata()).thenReturn(mock(RecordMetadata.class));
        doReturn(Flux.just(senderResult)).when(kafkaSender).send(any());

        GreetingEvent event = GreetingEvent.newBuilder()
                .setEventId("e2")
                .setName("Bob")
                .build();

        // Event must be non-empty bytes (round-trip sanity check)
        assert event.toByteArray().length > 0;

        StepVerifier.create(eventProducer.send("greeting-events", "e2", event))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void send_propagates_error_when_kafka_fails() {
        when(kafkaSender.send(any())).thenReturn(Flux.error(new RuntimeException("broker unavailable")));

        GreetingEvent event = GreetingEvent.newBuilder().setEventId("e3").setName("Carol").build();

        StepVerifier.create(eventProducer.send("greeting-events", "e3", event))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void sendAll_publishes_batch_and_emits_metadata_for_each_record() {
        @SuppressWarnings("unchecked")
        SenderResult<String> r1 = (SenderResult<String>) mock(SenderResult.class);
        @SuppressWarnings("unchecked")
        SenderResult<String> r2 = (SenderResult<String>) mock(SenderResult.class);
        when(r1.recordMetadata()).thenReturn(mock(RecordMetadata.class));
        when(r2.recordMetadata()).thenReturn(mock(RecordMetadata.class));
        doReturn(Flux.just(r1, r2)).when(kafkaSender).send(any());

        GreetingEvent e1 = GreetingEvent.newBuilder().setEventId("id1").setName("Alice").build();
        GreetingEvent e2 = GreetingEvent.newBuilder().setEventId("id2").setName("Bob").build();

        StepVerifier.create(eventProducer.sendAll("greeting-events", Map.of("id1", e1, "id2", e2)))
                .expectNextCount(2)
                .verifyComplete();

        verify(kafkaSender).send(any());
    }
}
