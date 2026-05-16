package com.aleoterob.transfer_consumer.infrastructure;

import com.aleoterob.transfer_consumer.domain.ProcessedTransfer;
import java.util.UUID;

public interface ProcessedTransferRepository {
	boolean existsById(UUID eventId);

	ProcessedTransfer save(ProcessedTransfer processedTransfer);
}
