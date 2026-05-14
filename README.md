# zero-message-loss

Demonstrates **guaranteed message delivery** in an event-driven banking system.

The core premise: a banking transfer event must **never be lost**, regardless of network failures, service crashes, or Kafka downtime. This is achieved by combining three patterns:

- **Transactional Outbox Pattern** — the transfer and its event are saved atomically in the same DB transaction. No dual-write problem.
- **Debezium CDC** — reads the Neon PostgreSQL WAL directly and publishes events to Kafka. If Kafka is temporarily down, Debezium retries automatically.
- **Kafka DLT (Dead Letter Topic)** — messages that exhaust all consumer retries land in a dedicated topic and are never silently dropped.

---

## Architecture

```
POST /transfers
      │
      ▼
transfer-producer  ──── Neon DB: transfer-producer-db ────► outbox_events table
      │                          (transfers table)               │
      │                                                          │ WAL (CDC)
      │                                                          ▼
      │                                                       Debezium
      │                                                          │
      │                                                          ▼
      │                                                        Kafka
      │                                               ┌─────────┴──────────┐
      │                                               ▼                    ▼
      │                                    transfers.created    transfers.created.DLT
      │                                               │                    │
      │                                    ┌──────────┘         ┌──────────┘
      │                                    ▼                    ▼
      │                             transfer-consumer      transfer-consumer
      │                             (processes event)      (DLT handler → logs)
      │                                    │
      │                             Neon DB: transfer-consumer-db
      │                             (processed_events table — idempotency)
      │
      └──────────────────────────────────────────────────────────────────────►
                                                              kafka-ui-service (Quarkus)
                                                              consumes both topics
                                                              exposes SSE endpoints
                                                                    │
                                                                    ▼
                                                              frontend (React + Vite)
                                                              real-time event viewer
```

---

## Services

| Service | Stack | Port | Database |
|---|---|---|---|
| `transfer-producer` | Spring Boot 4 + JDK 25 | 8081 | transfer-producer-db (Neon) |
| `transfer-consumer` | Spring Boot 4 + JDK 25 | 8082 | transfer-consumer-db (Neon) |
| `kafka-ui-service` | Quarkus 3.35 + JDK 25 | 8085 | none |
| `frontend` | React 19 + Vite 8 + TypeScript | 5173 | none |

---

## Infrastructure (Docker Compose)

| Container | Port | Role |
|---|---|---|
| `zookeeper` | 2181 | Kafka coordinator |
| `kafka` | 9092 (external) / 29092 (internal) | Message broker |
| `debezium` | 8083 | CDC connector — listens to Neon WAL |
| `kafka-ui` | 8080 | Kafka UI by provectuslabs — topic/message browser |

---

## How to Run

### Prerequisites
- Docker Desktop running
- JDK 21+
- Node.js 20+

### 1. Start infrastructure

```bash
cd C:\Projects\zero-message-loss
docker compose up -d
```

### 2. Enable logical replication in Neon

In Neon Console → project `transfer-producer-db` → Settings → Logical Replication → Enable.

Then run in Neon SQL Editor:

```sql
CREATE PUBLICATION transfer_publication FOR TABLE outbox_events;
```

### 3. Register Debezium connector

```bash
cd debezium
bash register-connector.sh
```

Verify:

```bash
curl http://localhost:8083/connectors/transfer-outbox-connector/status
```

### 4. Start transfer-producer

```bash
cd transfer-producer
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

### 5. Start transfer-consumer

```bash
cd transfer-consumer
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

### 6. Start kafka-ui-service

```bash
cd kafka-ui-service
./mvnw quarkus:dev
```

### 7. Start frontend

```bash
cd frontend
npm install
npm run dev
```

---

## Useful URLs

| URL | Description |
|---|---|
| http://localhost:8080 | Kafka UI — browse topics, messages, consumers |
| http://localhost:8081/transfers | transfer-producer REST API |
| http://localhost:8083/connectors | Debezium connector status |
| http://localhost:8085/events/stream | SSE stream — normal events |
| http://localhost:8085/events/dlt | SSE stream — DLT events |
| http://localhost:5173 | Frontend — real-time event viewer |

---

## Current Status ✅

- Monorepo initialized with Git and global `.gitignore`
- `docker-compose.yml` configured — Kafka, Zookeeper, Debezium, Kafka UI
- `debezium/register-connector.sh` — registers Neon WAL connector pointing to `transfer-producer-db` direct connection
- `transfer-producer` — Spring Boot project created with dependencies (Web, JPA, Kafka, Validation, PostgreSQL). `application.yaml` and `application-local.yaml` configured. Connects to `transfer-producer-db` on Neon. Runs on port 8081. Only entry point class exists, no business logic yet.
- `transfer-consumer` — Spring Boot project created with dependencies (JPA, Kafka, Validation, PostgreSQL). `application.yaml` and `application-local.yaml` configured. Connects to `transfer-consumer-db` on Neon. Runs on port 8082. Only entry point class exists, no business logic yet.
- `kafka-ui-service` — Quarkus project created with `quarkus-rest` and `quarkus-arc`. Only the generated `GreetingResource.java` exists. No Kafka or SSE dependencies yet.
- `frontend` — React 19 + Vite 8 + TypeScript scaffolded. Default Vite template, no custom code yet.

---

## Roadmap 🚀

Everything below is pending. Instructions are written for a coding agent.

---

### STEP 1 — Define Protobuf schema (shared between all services)

Create a `proto/` folder at the root of the monorepo:

```
zero-message-loss/
└── proto/
    └── transfer_event.proto
```

Content of `transfer_event.proto`:

```proto
syntax = "proto3";

package com.aleoterob.transfer;

option java_package = "com.aleoterob.transfer.proto";
option java_outer_classname = "TransferEventProto";
option java_multiple_files = true;

message TransferEvent {
  string event_id = 1;       // UUID — unique event identifier for idempotency
  string transfer_id = 2;    // UUID — the transfer this event belongs to
  string from_account = 3;   // source account number
  string to_account = 4;     // destination account number
  string amount = 5;         // decimal as string to avoid floating point issues
  string currency = 6;       // e.g. "ARS", "USD"
  string status = 7;         // "PENDING"
  int64 created_at = 8;      // epoch millis
}
```

Add `protobuf-java` to both Spring Boot `pom.xml` files and configure the `protobuf-maven-plugin` to generate Java classes from this `.proto` file into `target/generated-sources/protobuf`.

For `kafka-ui-service` (Quarkus), add `protobuf-java` and configure the same plugin. Also add `quarkus-smallrye-reactive-messaging-kafka` for Kafka consumption and `quarkus-rest-jackson` for SSE + JSON.

For `frontend`, install `protobufjs` or use plain JSON deserialization (kafka-ui-service will serialize the Protobuf payload to JSON before sending via SSE, so the frontend only needs to handle JSON).

---

### STEP 2 — transfer-producer

**Goal:** expose `POST /transfers`, save the transfer and an outbox event atomically, and let Debezium publish the event to Kafka.

#### 2.1 Configure JPA schema creation

The project intentionally lets Hibernate create/update the development schema when the service starts.

Keep this in `application.yaml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

#### 2.2 Tables generated from JPA entities

Create `Transfer` and `OutboxEvent` as JPA entities. On startup, Hibernate should create/update:

- `transfers`
- `outbox_events`

Use explicit `@Table` and `@Column` mappings so the generated schema matches Debezium expectations.

**Important:** after the app starts and Hibernate creates `outbox_events`, enable the Neon publication manually in the Neon SQL Editor:

```sql
CREATE PUBLICATION transfer_publication FOR TABLE outbox_events;
```

#### 2.3 Package structure

```
com.aleoterob.transfer_producer
├── api
│   └── TransferController.java
├── application
│   └── TransferService.java
├── domain
│   ├── Transfer.java         (JPA @Entity)
│   └── OutboxEvent.java      (JPA @Entity)
├── infrastructure
│   ├── TransferRepository.java
│   └── OutboxEventRepository.java
└── TransferProducerApplication.java
```

#### 2.4 TransferController

```java
@RestController
@RequestMapping("/transfers")
public class TransferController {
    @PostMapping
    public ResponseEntity<Transfer> create(@RequestBody @Valid CreateTransferRequest request) { ... }
}
```

`CreateTransferRequest` is a Java record:

```java
public record CreateTransferRequest(
  @NotBlank String fromAccount,
  @NotBlank String toAccount,
  @NotNull @DecimalMin("0.01") BigDecimal amount,
  @NotBlank String currency
) {}
```

#### 2.5 TransferService — the critical part

```java
@Service
@Transactional
public class TransferService {
  public Transfer create(CreateTransferRequest request) {
    // 1. Save transfer
    Transfer transfer = transferRepository.save(...);

    // 2. Serialize to Protobuf
    TransferEvent event = TransferEvent.newBuilder()
      .setEventId(UUID.randomUUID().toString())
      .setTransferId(transfer.getId().toString())
      .setFromAccount(transfer.getFromAccount())
      .setToAccount(transfer.getToAccount())
      .setAmount(transfer.getAmount().toPlainString())
      .setCurrency(transfer.getCurrency())
      .setStatus("PENDING")
      .setCreatedAt(Instant.now().toEpochMilli())
      .build();

    // 3. Save outbox event — same transaction
    OutboxEvent outbox = new OutboxEvent();
    outbox.setAggregateId(transfer.getId());
    outbox.setAggregateType("Transfer");
    outbox.setEventType("TransferCreated");
    outbox.setPayload(event.toByteArray()); // Protobuf bytes
    outboxEventRepository.save(outbox);

    return transfer;
    // Debezium will detect the outbox INSERT via WAL and publish to Kafka
  }
}
```

---

### STEP 3 — transfer-consumer

**Goal:** consume `transfers.created` from Kafka, deserialize Protobuf, check idempotency, process, and handle DLT.

#### 3.1 Configure JPA schema creation

Keep this in `application.yaml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

#### 3.2 Table generated from JPA entity

Create `ProcessedEvent` as a JPA entity. On startup, Hibernate should create/update:

- `processed_events`

#### 3.3 Package structure

```
com.aleoterob.transfer_consumer
├── application
│   ├── TransferConsumer.java     (@KafkaListener)
│   └── TransferDltConsumer.java  (@KafkaListener on DLT)
├── domain
│   └── ProcessedEvent.java       (JPA @Entity)
├── infrastructure
│   └── ProcessedEventRepository.java
└── TransferConsumerApplication.java
```

#### 3.4 TransferConsumer

```java
@Component
public class TransferConsumer {

  @KafkaListener(topics = "transfers.created", groupId = "transfer-consumer-group")
  public void consume(byte[] message) {
    TransferEvent event = TransferEvent.parseFrom(message); // deserialize Protobuf

    // Idempotency check
    UUID eventId = UUID.fromString(event.getEventId());
    if (processedEventRepository.existsById(eventId)) {
      log.warn("Duplicate event ignored: {}", eventId);
      return;
    }

    // Process (log for now, extend with business logic later)
    log.info("Processing transfer: {} → {} amount: {} {}",
      event.getFromAccount(), event.getToAccount(),
      event.getAmount(), event.getCurrency());

    // Mark as processed
    processedEventRepository.save(new ProcessedEvent(eventId, UUID.fromString(event.getTransferId())));
  }
}
```

#### 3.5 Retry and DLT configuration

Add to `application.yaml`:

```yaml
spring:
  kafka:
    consumer:
      group-id: transfer-consumer-group
      auto-offset-reset: earliest
      enable-auto-commit: false
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.ByteArrayDeserializer
    listener:
      ack-mode: manual
```

Create a `KafkaConfig.java` bean:

```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<String, byte[]> template) {
  DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
  FixedBackOff backOff = new FixedBackOff(2000L, 3L); // 3 retries, 2s apart
  return new DefaultErrorHandler(recoverer, backOff);
}
```

#### 3.6 DLT handler

```java
@Component
public class TransferDltConsumer {

  @KafkaListener(topics = "transfers.created.DLT", groupId = "transfer-dlt-group")
  public void handleDlt(byte[] message) {
    // Log the failed message — extend with alerting (Slack, PagerDuty) later
    log.error("Message landed in DLT. Raw bytes length: {}", message.length);
    // Attempt to deserialize for better logging
    try {
      TransferEvent event = TransferEvent.parseFrom(message);
      log.error("DLT event — transferId: {}, from: {}, to: {}, amount: {}",
        event.getTransferId(), event.getFromAccount(),
        event.getToAccount(), event.getAmount());
    } catch (Exception e) {
      log.error("Could not deserialize DLT message", e);
    }
  }
}
```

---

### STEP 4 — kafka-ui-service (Quarkus)

**Goal:** consume both Kafka topics and stream events to the frontend via SSE.

#### 4.1 Add dependencies to `pom.xml`

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-rest-jackson</artifactId>
</dependency>
<dependency>
  <groupId>com.google.protobuf</groupId>
  <artifactId>protobuf-java</artifactId>
  <version>4.29.3</version>
</dependency>
```

Remove `quarkus-rest` if present (replaced by `quarkus-rest-jackson`).

#### 4.2 Configure `application.properties`

```properties
quarkus.http.port=8085
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173

mp.messaging.incoming.transfers-created.connector=smallrye-kafka
mp.messaging.incoming.transfers-created.topic=transfers.created
mp.messaging.incoming.transfers-created.bootstrap.servers=localhost:9092
mp.messaging.incoming.transfers-created.value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer

mp.messaging.incoming.transfers-dlt.connector=smallrye-kafka
mp.messaging.incoming.transfers-dlt.topic=transfers.created.DLT
mp.messaging.incoming.transfers-dlt.bootstrap.servers=localhost:9092
mp.messaging.incoming.transfers-dlt.value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer
```

#### 4.3 Package structure

```
org.aleoterob
├── EventStreamResource.java   (SSE endpoints)
├── TransferEventMapper.java   (Protobuf → EventDto)
├── dto
│   └── TransferEventDto.java  (record — JSON payload for SSE)
└── messaging
    ├── TransferCreatedConsumer.java
    └── TransferDltConsumer.java
```

#### 4.4 SSE endpoints

```java
@Path("/events")
@Produces(MediaType.SERVER_SENT_EVENTS)
public class EventStreamResource {

  @Inject
  @Channel("transfers-created-stream")
  Multi<TransferEventDto> createdStream;

  @Inject
  @Channel("transfers-dlt-stream")
  Multi<TransferEventDto> dltStream;

  @GET
  @Path("/stream")
  public Multi<TransferEventDto> streamCreated() {
    return createdStream;
  }

  @GET
  @Path("/dlt")
  public Multi<TransferEventDto> streamDlt() {
    return dltStream;
  }
}
```

#### 4.5 TransferEventDto (record)

```java
public record TransferEventDto(
  String eventId,
  String transferId,
  String fromAccount,
  String toAccount,
  String amount,
  String currency,
  String status,
  long createdAt,
  boolean isDlt
) {}
```

---

### STEP 5 — frontend (React + Vite + TypeScript)

**Goal:** display real-time events from both SSE endpoints in a split-panel UI.

#### 5.1 Replace default Vite template

Delete all content from `App.tsx`, `App.css`, `index.css`. Start from scratch.

#### 5.2 What to show

Split-panel layout:

- **Left panel — Live Transfers** — events received from `GET /events/stream`
- **Right panel — Dead Letter Topic** — events received from `GET /events/dlt`

Each event card shows:
- `transferId` (truncated UUID)
- `fromAccount → toAccount`
- `amount` + `currency`
- `status`
- `createdAt` formatted as local time
- Color: green for normal, red for DLT

#### 5.3 SSE connection

Use native `EventSource` API:

```typescript
// hooks/useEventStream.ts
export function useEventStream(url: string) {
  const [events, setEvents] = useState<TransferEventDto[]>([]);

  useEffect(() => {
    const source = new EventSource(url);
    source.onmessage = (e) => {
      const event: TransferEventDto = JSON.parse(e.data);
      setEvents(prev => [event, ...prev]); // newest first
    };
    return () => source.close();
  }, [url]);

  return events;
}
```

#### 5.4 TransferEventDto TypeScript type

```typescript
export interface TransferEventDto {
  eventId: string;
  transferId: string;
  fromAccount: string;
  toAccount: string;
  amount: string;
  currency: string;
  status: string;
  createdAt: number;
  isDlt: boolean;
}
```

#### 5.5 No external dependencies needed

Do not install TanStack Query — SSE is handled natively with `EventSource` and `useState`. Keep the frontend dependency-free beyond React itself.

---

### STEP 6 — End-to-end validation

Once all services are running, verify the full flow:

1. `POST http://localhost:8081/transfers` with body `{ "fromAccount": "ACC001", "toAccount": "ACC002", "amount": 1500.00, "currency": "ARS" }`
2. Verify in Kafka UI (http://localhost:8080) that topic `transfers.created` received a message
3. Verify in `transfer-consumer` logs that the event was processed
4. Verify in `processed_events` table in Neon that the `event_id` was saved
5. Verify the event appears in the frontend left panel in real time
6. To test DLT: temporarily break the consumer (throw an exception in `TransferConsumer.consume`) and send another transfer — after 3 retries it should land in `transfers.created.DLT`
7. Verify the DLT event appears in the frontend right panel
8. To test idempotency: replay the same `event_id` — confirm the consumer logs "Duplicate event ignored" and does not insert into `processed_events` again
