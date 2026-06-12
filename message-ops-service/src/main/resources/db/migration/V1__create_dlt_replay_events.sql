CREATE TABLE IF NOT EXISTS dlt_replay_events (
    event_id varchar(36) PRIMARY KEY,
    transfer_id varchar(36) NOT NULL,
    event_key text,
    payload bytea NOT NULL,
    delivery_state varchar(32) NOT NULL,
    replay_attempts integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    last_attempt_at timestamptz,
    replayed_at timestamptz,
    confirmed_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_dlt_replay_events_delivery_state
    ON dlt_replay_events (delivery_state);

CREATE INDEX IF NOT EXISTS idx_dlt_replay_events_transfer_id
    ON dlt_replay_events (transfer_id);
