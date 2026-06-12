#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck source=/dev/null
  source "$ENV_FILE"
  set +a
fi

: "${TRANSFER_CONSUMER_DB_HOST:?TRANSFER_CONSUMER_DB_HOST is required}"
: "${TRANSFER_CONSUMER_DB_USER:?TRANSFER_CONSUMER_DB_USER is required}"
: "${TRANSFER_CONSUMER_DB_PASSWORD:?TRANSFER_CONSUMER_DB_PASSWORD is required}"

TRANSFER_CONSUMER_DB_PORT="${TRANSFER_CONSUMER_DB_PORT:-5432}"
TRANSFER_CONSUMER_DB_NAME="${TRANSFER_CONSUMER_DB_NAME:-neondb}"

echo "Waiting for Debezium..."
until curl -s http://localhost:8083/connectors > /dev/null; do
  sleep 2
done

curl -s -X DELETE http://localhost:8083/connectors/processed-events-connector > /dev/null || true

curl -X PUT http://localhost:8083/connectors/processed-transfers-connector/config -H "Content-Type: application/json" -d "{
    \"connector.class\": \"io.debezium.connector.postgresql.PostgresConnector\",
    \"database.hostname\": \"${TRANSFER_CONSUMER_DB_HOST}\",
    \"database.port\": \"${TRANSFER_CONSUMER_DB_PORT}\",
    \"database.user\": \"${TRANSFER_CONSUMER_DB_USER}\",
    \"database.password\": \"${TRANSFER_CONSUMER_DB_PASSWORD}\",
    \"database.dbname\": \"${TRANSFER_CONSUMER_DB_NAME}\",
    \"plugin.name\": \"pgoutput\",
    \"slot.name\": \"processed_transfers_slot\",
    \"publication.name\": \"processed_transfers_publication\",
    \"table.include.list\": \"public.processed_transfers\",
    \"decimal.handling.mode\": \"string\",
    \"topic.prefix\": \"transfer-consumer-db\",
    \"tombstones.on.delete\": \"false\",
    \"transforms\": \"route\",
    \"transforms.route.type\": \"org.apache.kafka.connect.transforms.RegexRouter\",
    \"transforms.route.regex\": \"transfer-consumer-db.public.processed_transfers\",
    \"transforms.route.replacement\": \"consumer.processed-transfers\",
    \"value.converter\": \"org.apache.kafka.connect.json.JsonConverter\",
    \"value.converter.schemas.enable\": \"false\",
    \"key.converter\": \"org.apache.kafka.connect.storage.StringConverter\"
}"
