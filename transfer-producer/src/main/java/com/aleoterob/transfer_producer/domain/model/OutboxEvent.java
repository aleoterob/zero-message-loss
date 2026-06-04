package com.aleoterob.transfer_producer.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId;

	@Column(name = "aggregate_type", nullable = false, length = 64)
	private String aggregateType;

	@Column(name = "event_type", nullable = false, length = 64)
	private String eventType;

	@Column(nullable = false, columnDefinition = "bytea")
	private byte[] payload;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected OutboxEvent() {
	}

	public static OutboxEvent create(UUID aggregateId, String aggregateType, String eventType, byte[] payload) {
		OutboxEvent event = new OutboxEvent();
		event.id = UUID.randomUUID();
		event.aggregateId = aggregateId;
		event.aggregateType = aggregateType;
		event.eventType = eventType;
		event.payload = payload;
		event.createdAt = Instant.now();
		return event;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAggregateId() {
		return aggregateId;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public String getEventType() {
		return eventType;
	}

	public byte[] getPayload() {
		return payload;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
