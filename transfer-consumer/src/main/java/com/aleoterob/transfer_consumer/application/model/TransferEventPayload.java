package com.aleoterob.transfer_consumer.application.model;

import com.aleoterob.transfer_consumer.domain.model.ProcessedTransfer;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferEventPayload(
		UUID eventId,
		UUID transferId,
		String fromAccount,
		String toAccount,
		BigDecimal amount,
		String currency,
		String status,
		long createdAt) {

	public TransferEventPayload markProcessed() {
		return new TransferEventPayload(
				eventId,
				transferId,
				fromAccount,
				toAccount,
				amount,
				currency,
				ProcessedTransfer.PROCESSED_STATUS,
				createdAt);
	}
}
