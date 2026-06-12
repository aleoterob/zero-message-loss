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

: "${TRANSFER_PRODUCER_DB_HOST:?TRANSFER_PRODUCER_DB_HOST is required}"
: "${TRANSFER_PRODUCER_DB_USER:?TRANSFER_PRODUCER_DB_USER is required}"
: "${TRANSFER_PRODUCER_DB_PASSWORD:?TRANSFER_PRODUCER_DB_PASSWORD is required}"

TRANSFER_PRODUCER_DB_PORT="${TRANSFER_PRODUCER_DB_PORT:-5432}"
TRANSFER_PRODUCER_DB_NAME="${TRANSFER_PRODUCER_DB_NAME:-neondb}"

echo "Waiting for Debezium..."
until curl -s http://localhost:8083/connectors > /dev/null; do
  sleep 2
done

curl -X PUT http://localhost:8083/connectors/transfer-outbox-connector/config -H "Content-Type: application/json" -d "{
    \"connector.class\": \"io.debezium.connector.postgresql.PostgresConnector\",
    \"database.hostname\": \"${TRANSFER_PRODUCER_DB_HOST}\",
    \"database.port\": \"${TRANSFER_PRODUCER_DB_PORT}\",
    \"database.user\": \"${TRANSFER_PRODUCER_DB_USER}\",
    \"database.password\": \"${TRANSFER_PRODUCER_DB_PASSWORD}\",
    \"database.dbname\": \"${TRANSFER_PRODUCER_DB_NAME}\",
    \"binary.handling.mode\": \"base64\",
    \"plugin.name\": \"pgoutput\",
    \"slot.name\": \"debezium_slot\",
    \"publication.name\": \"transfer_publication\",
    \"table.include.list\": \"public.outbox_events\",
    \"topic.prefix\": \"zero-message-loss\",
    \"transforms\": \"outbox\",
    \"transforms.outbox.type\": \"io.debezium.transforms.outbox.EventRouter\",
    \"transforms.outbox.table.field.event.key\": \"aggregate_id\",
    \"transforms.outbox.table.field.event.type\": \"event_type\",
    \"transforms.outbox.table.field.event.payload\": \"payload\",
    \"transforms.outbox.route.by.field\": \"aggregate_type\",
    \"transforms.outbox.route.topic.replacement\": \"transfers.created\",
    \"value.converter\": \"org.apache.kafka.connect.storage.StringConverter\",
    \"key.converter\": \"org.apache.kafka.connect.storage.StringConverter\"
}"
