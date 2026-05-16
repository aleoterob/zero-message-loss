# zero-message-loss

Demo de **entrega garantizada de mensajes** para transferencias bancarias usando Transactional Outbox, Debezium CDC, Kafka, DLT, replay automatico y confirmacion en base de datos del consumer.

La idea central es simple: cuando se crea una transferencia, el sistema guarda la transferencia y su evento en la misma transaccion de base de datos. Debezium lee el outbox desde el WAL de Neon y publica el evento en Kafka. Si el consumer falla, Spring Kafka envia el mensaje a un Dead Letter Topic. Luego `message-ops-service` hace replay automatico cuando el consumer vuelve a estar sano, y el frontend muestra en tiempo real cuando el evento fue finalmente persistido en `transfer-consumer-db.processed_transfers`.

---

## Architecture

```text
frontend
  | POST /transfers
  v
transfer-producer (8081)
  | same DB transaction
  |-- transfers
  `-- outbox_events
        |
        | Debezium connector: transfer-outbox-connector
        v
Kafka topic: transfers.created
        |
        v
transfer-consumer (8082)
  | success
  v
transfer-consumer-db.processed_transfers
        |
        | Debezium connector: processed-transfers-connector
        v
Kafka topic: consumer.processed-transfers
        |
        v
message-ops-service (8085) -> SSE /events/processed -> frontend

Failure path:

transfers.created
  -> transfer-consumer retries
  -> transfers.created.DLT
  -> message-ops-service
  -> automatic replay to transfers.created when consumer is healthy
```

---

## Services

| Service | Stack | Port | Responsibility |
|---|---|---:|---|
| `transfer-producer` | Spring Boot 4 + JDK 25 | 8081 | Creates transfers and writes outbox events atomically |
| `transfer-consumer` | Spring Boot 4 + JDK 25 | 8082 | Consumes `transfers.created`, persists `processed_transfers`, exposes demo control endpoints |
| `message-ops-service` | Quarkus 3.35 + JDK 25 | 8085 | Streams Kafka events to the frontend, replays DLT events, proxies consumer controls |
| `frontend` | React 19 + Vite 8 + shadcn/ui | 5173 | Demo dashboard: create transfers, trigger failures, watch live/DLT/DB confirmation |

## Infrastructure

| Container | Port | Responsibility |
|---|---:|---|
| `zookeeper` | 2181 | Kafka coordination |
| `kafka` | 9092 external / 29092 internal | Message broker |
| `debezium` | 8083 | Kafka Connect + Debezium Postgres connectors |
| `kafka-ui` | 8080 | Kafka topic and consumer browser |

---

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `transfers.created` | Debezium outbox connector, `message-ops-service` replay | `transfer-consumer`, `message-ops-service` |
| `transfers.created.DLT` | `transfer-consumer` error handler | `transfer-consumer` DLT logger, `message-ops-service` |
| `consumer.processed-transfers` | Debezium processed-transfers connector | `message-ops-service` |

---

## Databases

### `transfer-producer-db`

Hibernate creates/updates:

- `transfers`
- `outbox_events`

Debezium reads `outbox_events` and publishes the Protobuf payload to `transfers.created`.

### `transfer-consumer-db`

Hibernate creates/updates:

- `processed_transfers`

Debezium reads `processed_transfers` and publishes database confirmations to `consumer.processed-transfers`.

---

## Shared Event Schema

All backend services generate Java classes from [proto/transfer_event.proto](proto/transfer_event.proto):

```proto
message TransferEvent {
  string event_id = 1;
  string transfer_id = 2;
  string from_account = 3;
  string to_account = 4;
  string amount = 5;
  string currency = 6;
  string status = 7;
  int64 created_at = 8;
}
```

`event_id` is the idempotency key used by `transfer-consumer`.

---

## Setup

### 1. Start infrastructure

```bash
docker compose up -d
```

### 2. Enable logical replication in Neon

Enable **Logical Replication** in both Neon projects:

- `transfer-producer-db`
- `transfer-consumer-db`

### 3. Create publications

Run in `transfer-producer-db`:

```sql
CREATE PUBLICATION transfer_publication FOR TABLE outbox_events;
```

Run in `transfer-consumer-db`:

```sql
CREATE PUBLICATION processed_transfers_publication
FOR TABLE public.processed_transfers;
```

### 4. Register Debezium connectors

```bash
cd debezium
bash register-producer-connector.sh
bash register-consumer-connector.sh
```

Verify:

```bash
curl http://localhost:8083/connectors/transfer-outbox-connector/status
curl http://localhost:8083/connectors/processed-transfers-connector/status
```

Both tasks should be `RUNNING`.

### 5. Start services

```bash
cd transfer-producer
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

```bash
cd transfer-consumer
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

```bash
cd message-ops-service
./mvnw quarkus:dev
```

```bash
cd frontend
npm install
npm run dev
```

---

## Useful URLs

| URL | Description |
|---|---|
| http://localhost:5173 | Frontend dashboard |
| http://localhost:8080 | Kafka UI |
| http://localhost:8081/transfers | Transfer producer API |
| http://localhost:8082/consumer/status | Transfer consumer status |
| http://localhost:8083/connectors | Debezium connectors |
| http://localhost:8085/events/stream | SSE live transfer events |
| http://localhost:8085/events/dlt | SSE DLT/replay events |
| http://localhost:8085/events/processed | SSE consumer DB confirmations |
| http://localhost:8085/consumer/status | Message ops proxy for consumer status |

---

## Runtime Flow

### Normal transfer

1. The frontend sends `POST /transfers` to `transfer-producer`.
2. `TransferService` inserts `transfers` and `outbox_events` in one transaction.
3. Debezium reads `outbox_events` and publishes to `transfers.created`.
4. `transfer-consumer` parses the Protobuf event and inserts `processed_transfers`.
5. Debezium reads `processed_transfers` and publishes to `consumer.processed-transfers`.
6. `message-ops-service` streams the DB confirmation to the frontend.

### DLT + automatic recovery

1. Enable failure mode from the frontend.
2. Create a transfer.
3. `transfer-consumer` throws during processing.
4. Spring Kafka retries and sends the message to `transfers.created.DLT`.
5. `message-ops-service` shows the event as `DLT_PENDING`.
6. Restore processing from the frontend.
7. `message-ops-service` replays the original payload to `transfers.created`.
8. The card changes to `DLT_REPLAYED`.
9. When `processed_transfers` is written in `transfer-consumer-db`, the frontend shows `Consumer DB confirmed`.

---

## Transfer Consumer Demo Controls

`transfer-consumer` exposes:

```http
GET  /consumer/status
POST /consumer/fail-processing
POST /consumer/restore-processing
```

The frontend calls these through `message-ops-service` at:

```http
GET  /consumer/status
POST /consumer/fail-processing
POST /consumer/restore-processing
```

`fail-processing` intentionally throws inside the listener so Kafka retries and eventually sends the message to DLT. `restore-processing` turns processing back on so `message-ops-service` can replay pending DLT messages automatically.

---

## Frontend Panels

- **Top panel:** create transfer, enable/restore failure mode.
- **Live Transfers:** events observed on `transfers.created`.
- **Dead Letter Topic / Replay:** events from `transfers.created.DLT`, replay state, and the matching consumer DB transfer confirmation.

State meanings:

| State | Meaning |
|---|---|
| `LIVE` | Event observed on the normal Kafka topic |
| `DLT_PENDING` | Event is in the dead letter topic and waiting for replay |
| `DLT_REPLAYED` | Event was republished to `transfers.created` |
| `Consumer DB confirmed` | Matching `processed_transfers` row was inserted with the transfer details |

---

## Tests And Checks

Backend:

```bash
cd transfer-producer
./mvnw test

cd transfer-consumer
./mvnw test

cd message-ops-service
./mvnw test
```

Frontend:

```bash
cd frontend
npm run lint
npm run build
npx -y react-doctor@latest . --verbose --diff
```

---

## Troubleshooting

If a Debezium connector task fails with:

```text
logical decoding requires "wal_level" >= "logical"
```

Enable Logical Replication in the corresponding Neon project and rerun the connector script.

If a connector already exists, the scripts use `PUT`, so they can be run again safely:

```bash
bash debezium/register-producer-connector.sh
bash debezium/register-consumer-connector.sh
```

The consumer connector script also removes the old `processed-events-connector` name before registering `processed-transfers-connector`.

If VS Code shows a Maven error for:

```text
com.google.protobuf:protoc:exe:${os.detected.classifier}
```

but Maven works from the terminal, run:

- `Java: Clean Java Language Server Workspace`
- `Maven: Reload All Maven Projects`
