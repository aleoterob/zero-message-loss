package com.aleoterob.transfer_consumer.infrastructure;

import com.aleoterob.transfer_consumer.domain.ProcessedEvent;
import java.util.UUID;

public interface ProcessedEventRepository {
	boolean existsById(UUID eventId);

	ProcessedEvent save(ProcessedEvent processedEvent);
}
