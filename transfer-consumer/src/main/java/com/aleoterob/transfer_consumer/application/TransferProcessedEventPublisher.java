package com.aleoterob.transfer_consumer.application;

import com.aleoterob.transfer.proto.TransferEvent;

public interface TransferProcessedEventPublisher {
	void publish(TransferEvent event);
}
