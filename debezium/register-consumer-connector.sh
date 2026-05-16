#!/bin/bash

echo "Waiting for Debezium..."
until curl -s http://localhost:8083/connectors > /dev/null; do
  sleep 2
done

curl -X PUT http://localhost:8083/connectors/processed-events-connector/config -H "Content-Type: application/json" -d '{
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "ep-royal-thunder-actfaxh5.sa-east-1.aws.neon.tech",
    "database.port": "5432",
    "database.user": "neondb_owner",
    "database.password": "npg_coPVdtEN6X0p",
    "database.dbname": "neondb",
    "plugin.name": "pgoutput",
    "slot.name": "processed_events_slot",
    "publication.name": "processed_events_publication",
    "table.include.list": "public.processed_events",
    "topic.prefix": "transfer-consumer-db",
    "tombstones.on.delete": "false",
    "transforms": "route",
    "transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter",
    "transforms.route.regex": "transfer-consumer-db.public.processed_events",
    "transforms.route.replacement": "consumer.processed-events",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'
