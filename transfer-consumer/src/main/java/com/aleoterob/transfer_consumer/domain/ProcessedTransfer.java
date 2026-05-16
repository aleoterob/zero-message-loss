package com.aleoterob.transfer_consumer.domain;

import com.aleoterob.transfer.proto.TransferEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_transfers")
public class ProcessedTransfer {
	private static final String PROCESSED_STATUS = "PROCESSED";

	@Id
	@Column(name = "event_id", nullable = false, updatable = false)
	private UUID eventId;

	@Column(name = "transfer_id", nullable = false, updatable = false)
	private UUID transferId;

	@Column(name = "from_account", nullable = false, updatable = false, length = 64)
	private String fromAccount;

	@Column(name = "to_account", nullable = false, updatable = false, length = 64)
	private String toAccount;

	@Column(nullable = false, updatable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(nullable = false, updatable = false, length = 8)
	private String currency;

	@Column(nullable = false, updatable = false, length = 32)
	private String status;

	@Column(name = "processed_at", nullable = false, updatable = false)
	private Instant processedAt;

	protected ProcessedTransfer() {
	}

	public static ProcessedTransfer create(TransferEvent event) {
		ProcessedTransfer processedTransfer = new ProcessedTransfer();
		processedTransfer.eventId = UUID.fromString(event.getEventId());
		processedTransfer.transferId = UUID.fromString(event.getTransferId());
		processedTransfer.fromAccount = event.getFromAccount();
		processedTransfer.toAccount = event.getToAccount();
		processedTransfer.amount = new BigDecimal(event.getAmount());
		processedTransfer.currency = event.getCurrency();
		processedTransfer.status = PROCESSED_STATUS;
		processedTransfer.processedAt = Instant.now();
		return processedTransfer;
	}

	public UUID getEventId() {
		return eventId;
	}

	public UUID getTransferId() {
		return transferId;
	}

	public String getFromAccount() {
		return fromAccount;
	}

	public String getToAccount() {
		return toAccount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public String getStatus() {
		return status;
	}

	public Instant getProcessedAt() {
		return processedAt;
	}
}
