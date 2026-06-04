package com.aleoterob.transfer_producer.application.ports.output;

import com.aleoterob.transfer_producer.domain.model.OutboxEvent;

public interface OutboxEventRepository {
	OutboxEvent save(OutboxEvent outboxEvent);
}
