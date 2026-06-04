package com.aleoterob.transfer_consumer.application.ports.output;

import com.aleoterob.transfer_consumer.domain.model.ProcessedTransfer;
import java.util.UUID;

public interface ProcessedTransferRepository {
	boolean existsById(UUID eventId);

	ProcessedTransfer save(ProcessedTransfer processedTransfer);
}
