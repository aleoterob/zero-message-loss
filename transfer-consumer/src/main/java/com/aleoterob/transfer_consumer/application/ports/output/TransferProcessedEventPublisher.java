package com.aleoterob.transfer_consumer.application.ports.output;

import com.aleoterob.transfer_consumer.application.model.TransferEventPayload;

public interface TransferProcessedEventPublisher {
	void publish(TransferEventPayload event);
}
