package com.flipfoundry.tutorial.application.kafka.producer;

import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.util.List;
import java.util.Map;

/**
 * Generic async Kafka producer for Protobuf messages.
 *
 * <p>Serializes any {@link Message} to raw bytes and publishes it to the specified
 * Kafka topic using a non-blocking, reactive {@link KafkaSender}.
 *
 * <p>Usage example:
 * <pre>{@code
 *   GreetingEvent event = GreetingEvent.newBuilder()
 *       .setEventId(UUID.randomUUID().toString())
 *       .setName("Alice")
 *       .build();
 *   eventProducer.send("greeting-events", event.getEventId(), event).subscribe();
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(KafkaSender.class)
public class EventProducer {

    private final KafkaSender<String, byte[]> kafkaSender;

    /**
     * Asynchronously sends a single Protobuf message to a Kafka topic.
     *
     * @param topic  the target Kafka topic
     * @param key    the partition key (typically a domain entity ID)
     * @param event  the Protobuf message to publish
     * @return a {@link Mono} completing with the {@link RecordMetadata} on success
     */
    public <T extends Message> Mono<RecordMetadata> send(String topic, String key, T event) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, event.toByteArray());
        return kafkaSender
                .send(Mono.just(SenderRecord.create(record, key)))
                .single()
                .map(SenderResult::recordMetadata)
                .doOnSuccess(meta -> log.debug(
                        "Published {}:{} → {}[{}]@{}",
                        key, event.getClass().getSimpleName(),
                        meta.topic(), meta.partition(), meta.offset()))
                .doOnError(e -> log.error(
                        "Failed to publish {}:{} to {}", key, event.getClass().getSimpleName(), topic, e));
    }

    /**
     * Asynchronously sends a batch of Protobuf messages to a Kafka topic.
     *
     * @param topic   the target Kafka topic
     * @param events  a map of partition key → Protobuf message
     * @return a {@link Flux} of {@link RecordMetadata} for each published record
     */
    public <T extends Message> Flux<RecordMetadata> sendAll(String topic, Map<String, T> events) {
        List<SenderRecord<String, byte[], String>> records = events.entrySet().stream()
                .map(e -> SenderRecord.create(
                        new ProducerRecord<>(topic, e.getKey(), e.getValue().toByteArray()),
                        e.getKey()))
                .toList();

        return kafkaSender
                .send(Flux.fromIterable(records))
                .map(SenderResult::recordMetadata)
                .doOnNext(meta -> log.debug("Batch published → {}[{}]@{}", meta.topic(), meta.partition(), meta.offset()))
                .doOnError(e -> log.error("Failed to publish batch to {}", topic, e));
    }
}
