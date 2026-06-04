package com.aleoterob.transfer_producer.application.ports.input;

import java.util.UUID;

public interface MarkTransferProcessedUseCase {
	void markProcessed(UUID transferId);
}
