package com.aleoterob.transfer_consumer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
	@Id
	@Column(name = "event_id", nullable = false, updatable = false)
	private UUID eventId;

	@Column(name = "transfer_id", nullable = false, updatable = false)
	private UUID transferId;

	@Column(name = "processed_at", nullable = false, updatable = false)
	private Instant processedAt;

	protected ProcessedEvent() {
	}

	public static ProcessedEvent create(UUID eventId, UUID transferId) {
		ProcessedEvent processedEvent = new ProcessedEvent();
		processedEvent.eventId = eventId;
		processedEvent.transferId = transferId;
		processedEvent.processedAt = Instant.now();
		return processedEvent;
	}

	public UUID getEventId() {
		return eventId;
	}

	public UUID getTransferId() {
		return transferId;
	}

	public Instant getProcessedAt() {
		return processedAt;
	}
}
