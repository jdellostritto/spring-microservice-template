package com.flipfoundry.tutorial.application.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the kafka-producer module.
 *
 * <p>Defaults are defined here in the module. The consuming application can
 * override any value in its own {@code application.yml}:
 * <pre>
 * kafka:
 *   producer:
 *     event-topic: my-custom-topic
 * </pre>
 */
@ConfigurationProperties(prefix = "kafka.producer")
public class KafkaProducerProperties {

    /** Target topic for all domain events. */
    private String eventTopic = "event-topic";

    public String getEventTopic() {
        return eventTopic;
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }
}
