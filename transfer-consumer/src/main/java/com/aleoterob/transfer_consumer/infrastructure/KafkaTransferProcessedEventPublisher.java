package com.aleoterob.transfer_consumer.infrastructure;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_consumer.application.TransferProcessedEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransferProcessedEventPublisher implements TransferProcessedEventPublisher {
	public static final String TRANSFERS_PROCESSED_TOPIC = "transfers.processed";

	private final KafkaTemplate<String, byte[]> kafkaTemplate;

	public KafkaTransferProcessedEventPublisher(KafkaTemplate<String, byte[]> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void publish(TransferEvent event) {
		kafkaTemplate.send(TRANSFERS_PROCESSED_TOPIC, event.getTransferId(), event.toByteArray());
	}
}
