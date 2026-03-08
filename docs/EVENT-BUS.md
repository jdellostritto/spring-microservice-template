# Event / Data Bus

> **Convention:** Reactive Kafka producer module — `EventProducer` publishes Protobuf-serialized domain events. Enabled per-profile via `kafka.producer.enabled=true`. Event schemas live in `modules/kafka-producer/src/main/proto/`.

---

## Module Structure

The event bus is implemented as a self-contained Gradle subproject at `modules/kafka-producer`.

```
modules/kafka-producer/
├── build.gradle                          # java-library with reactor-kafka + protobuf-java
└── src/main/
    ├── java/.../kafka/
    │   ├── config/
    │   │   ├── KafkaProducerConfig.java           # @ConditionalOnProperty — beans for KafkaSender
    │   │   ├── KafkaProducerProperties.java        # @ConfigurationProperties(prefix="kafka.producer")
    │   │   └── KafkaProducerPropertiesConfig.java  # Always-on: registers properties bean
    │   └── producer/
    │       └── EventProducer.java                 # Generic reactive Protobuf publisher
    └── proto/
        └── events.proto                           # GreetingEvent + DepartingEvent schemas
```

The module exposes `protobuf-java` as an `api` dependency so consuming applications can build and read proto messages without re-declaring the dependency.

---

## Activation

The producer is **disabled by default**. Enable it in the profile that has a real broker:

```yaml
# application-deploy.yml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}

kafka:
  producer:
    enabled: true
```

When `kafka.producer.enabled` is absent or `false`, `KafkaProducerConfig` is skipped and no `KafkaSender` or `EventProducer` bean is created. `KafkaProducerProperties` is always registered so topic defaults are available everywhere.

---

## Configuration Properties

```yaml
kafka:
  producer:
    enabled: true           # activates the producer beans (default: false)
    event-topic: event-topic  # target topic (default: "event-topic")
```

| Property | Default | Description |
|---|---|---|
| `kafka.producer.enabled` | `false` | Activates `KafkaProducerConfig` and `EventProducer` |
| `kafka.producer.event-topic` | `event-topic` | Default topic used by `KafkaProducerProperties.getEventTopic()` |
| `spring.kafka.bootstrap-servers` | _(required when enabled)_ | Broker connection string |

---

## Producer Settings

`KafkaProducerConfig` builds a `KafkaSender<String, byte[]>` with the following guarantees:

| Setting | Value | Purpose |
|---|---|---|
| `enable.idempotence` | `true` | Exactly-once delivery per partition |
| `acks` | `all` | All in-sync replicas must acknowledge |
| `retries` | `Integer.MAX_VALUE` | Retry until success |
| `max.in.flight.requests.per.connection` | `5` | Maximum compatible with idempotence |
| `compression.type` | `snappy` | Wire-format compression |
| `linger.ms` | `5` | Batch accumulation window |
| `batch.size` | `32 KB` | Maximum batch before flush |
| Key serializer | `StringSerializer` | Partition keys are strings |
| Value serializer | `ByteArraySerializer` | Payload is raw proto bytes |

---

## Event Schema

Schemas are defined in `modules/kafka-producer/src/main/proto/events.proto` and compiled to Java by the Protobuf Gradle plugin.

```protobuf
message GreetingEvent {
  string event_id  = 1;
  string name      = 2;
  string message   = 3;
  string locale    = 4;
  google.protobuf.Timestamp occurred_at = 5;
}

message DepartingEvent {
  string event_id  = 1;
  string name      = 2;
  string farewell  = 3;
  google.protobuf.Timestamp occurred_at = 4;
}
```

All events share the same fields:
- `event_id` — random UUID, unique per publish call
- `occurred_at` — wall-clock `Timestamp` set at publish time

---

## EventProducer API

`EventProducer` is a generic, protocol-agnostic publisher. It accepts any `com.google.protobuf.Message` and delegates transport to the underlying `KafkaSender`.

```java
// Single event
eventProducer.send(topic, partitionKey, protoMessage)
    .subscribe();

// Batch
eventProducer.sendAll(topic, Map.of(key1, event1, key2, event2))
    .subscribe();
```

| Method | Returns | Description |
|---|---|---|
| `send(topic, key, event)` | `Mono<RecordMetadata>` | Publishes one message asynchronously |
| `sendAll(topic, events)` | `Flux<RecordMetadata>` | Publishes a batch; one record per map entry |

---

## Writing a Domain Event Publisher

Domain services must not call `EventProducer` directly. Wrap it in a dedicated publisher class that owns the domain-to-proto mapping:

```java
@Slf4j
@Service
public class GreetingEventPublisher {

    @Autowired(required = false)
    private EventProducer eventProducer;          // null when kafka.producer.enabled=false

    @Autowired
    private KafkaProducerProperties kafkaProducerProperties;

    public Mono<Void> publishGreeting(String name, String message, String locale) {
        if (eventProducer == null) {
            log.debug("Kafka disabled — skipping GreetingEvent for {}", name);
            return Mono.empty();
        }
        String topic = kafkaProducerProperties.getEventTopic();
        GreetingEvent event = GreetingEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setName(name)
                .setMessage(message)
                .setLocale(locale)
                .setOccurredAt(/* current Timestamp */)
                .build();
        return eventProducer.send(topic, event.getEventId(), event).then();
    }
}
```

**Rules:**
- Inject `EventProducer` with `required = false` — the bean does not exist when Kafka is disabled
- Guard with a null check and return `Mono.empty()` to keep the reactive chain intact
- Build the proto message in the publisher, not in the service layer
- Use `event_id` as the partition key to ensure at-most-one partition per logical event

---

## Adding a New Event Type

1. Add the message definition to `events.proto`
2. Run `./gradlew :modules:kafka-producer:generateProto` (or `make build`) to compile the Java classes
3. Create a publisher class in `application/events/` following the pattern above
4. Call the publisher from the relevant application service method
