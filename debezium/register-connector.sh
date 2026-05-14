#!/bin/bash

echo "Waiting for Debezium..."
until curl -s http://localhost:8083/connectors > /dev/null; do
  sleep 2
done

curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d '{
  "name": "transfer-outbox-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "ep-twilight-paper-acadq57u.sa-east-1.aws.neon.tech",
    "database.port": "5432",
    "database.user": "neondb_owner",
    "database.password": "npg_9tASuviGJ2EI",
    "database.dbname": "neondb",
    "plugin.name": "pgoutput",
    "slot.name": "debezium_slot",
    "publication.name": "transfer_publication",
    "table.include.list": "public.outbox_events",
    "topic.prefix": "zero-message-loss",
    "transforms": "outbox",
    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
    "transforms.outbox.table.field.event.key": "aggregate_id",
    "transforms.outbox.table.field.event.type": "event_type",
    "transforms.outbox.table.field.event.payload": "payload",
    "transforms.outbox.route.topic.replacement": "transfers.created",
    "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
  }
}'
