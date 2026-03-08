package com.flipfoundry.tutorial.application.kafka.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Always-on configuration that registers {@link KafkaProducerProperties}
 * regardless of whether {@code kafka.producer.enabled} is set.
 * This ensures topic defaults are available even when Kafka is disabled.
 */
@Configuration
@EnableConfigurationProperties(KafkaProducerProperties.class)
public class KafkaProducerPropertiesConfig {
}
