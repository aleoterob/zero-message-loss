package com.aleoterob.transfer_producer.infrastructure;

import com.aleoterob.transfer_producer.domain.OutboxEvent;

public interface OutboxEventRepository {
	OutboxEvent save(OutboxEvent outboxEvent);
}
