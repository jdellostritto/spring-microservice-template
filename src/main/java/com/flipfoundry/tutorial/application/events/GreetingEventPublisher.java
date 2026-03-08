package com.flipfoundry.tutorial.application.events;

import com.flipfoundry.tutorial.application.kafka.config.KafkaProducerProperties;
import com.flipfoundry.tutorial.application.kafka.producer.EventProducer;
import com.flipfoundry.tutorial.events.DepartingEvent;
import com.flipfoundry.tutorial.events.GreetingEvent;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event publisher for greeting and departing pipeline events.
 *
 * <p>Translates application domain data into typed Protobuf messages and
 * delegates asynchronous publishing to the generic {@link EventProducer}
 * from the kafka-producer module. This class owns the mapping from
 * domain concepts to wire-format events; the module owns only the
 * Kafka transport.
 *
 * <p>Only created when {@code kafka.producer.enabled=true} causes an
 * {@link EventProducer} bean to be present.
 */
@Slf4j
@Service
public class GreetingEventPublisher {

    /** Injected only when kafka.producer.enabled=true; null otherwise. */
    @Autowired(required = false)
    private EventProducer eventProducer;

    @Autowired
    private KafkaProducerProperties kafkaProducerProperties;

    /**
     * Publishes a {@link GreetingEvent} to the greeting-events topic.
     *
     * @param name    the recipient name
     * @param message the greeting message body
     * @param locale  the locale used for the greeting
     * @return a {@link Mono} completing empty on success
     */
    public Mono<Void> publishGreeting(String name, String message, String locale) {
        if (eventProducer == null) {
            log.debug("Kafka disabled — skipping GreetingEvent for {}", name);
            return Mono.empty();
        }
        String topic = kafkaProducerProperties.getEventTopic();
        Instant now = Instant.now();
        GreetingEvent event = GreetingEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setName(name)
                .setMessage(message)
                .setLocale(locale)
                .setOccurredAt(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build();
        return eventProducer.send(topic, event.getEventId(), event).then();
    }

    /**
     * Publishes a {@link DepartingEvent} to the departing-events topic.
     *
     * @param name     the person's name
     * @param farewell the farewell message
     * @return a {@link Mono} completing empty on success
     */
    public Mono<Void> publishDeparting(String name, String farewell) {
        if (eventProducer == null) {
            log.debug("Kafka disabled — skipping DepartingEvent");
            return Mono.empty();
        }
        String topic = kafkaProducerProperties.getEventTopic();
        Instant now = Instant.now();
        DepartingEvent event = DepartingEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setName(name)
                .setFarewell(farewell)
                .setOccurredAt(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build();
        return eventProducer.send(topic, event.getEventId(), event).then();
    }
}
