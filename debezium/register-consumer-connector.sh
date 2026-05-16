#!/bin/bash

echo "Waiting for Debezium..."
until curl -s http://localhost:8083/connectors > /dev/null; do
  sleep 2
done

curl -s -X DELETE http://localhost:8083/connectors/processed-events-connector > /dev/null || true

curl -X PUT http://localhost:8083/connectors/processed-transfers-connector/config -H "Content-Type: application/json" -d '{
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "ep-royal-thunder-actfaxh5.sa-east-1.aws.neon.tech",
    "database.port": "5432",
    "database.user": "neondb_owner",
    "database.password": "npg_coPVdtEN6X0p",
    "database.dbname": "neondb",
    "plugin.name": "pgoutput",
    "slot.name": "processed_transfers_slot",
    "publication.name": "processed_transfers_publication",
    "table.include.list": "public.processed_transfers",
    "decimal.handling.mode": "string",
    "topic.prefix": "transfer-consumer-db",
    "tombstones.on.delete": "false",
    "transforms": "route",
    "transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter",
    "transforms.route.regex": "transfer-consumer-db.public.processed_transfers",
    "transforms.route.replacement": "consumer.processed-transfers",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'
