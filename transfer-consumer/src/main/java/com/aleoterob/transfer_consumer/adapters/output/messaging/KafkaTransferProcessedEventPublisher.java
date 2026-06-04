package com.aleoterob.transfer_consumer.adapters.output.messaging;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_consumer.application.model.TransferEventPayload;
import com.aleoterob.transfer_consumer.application.ports.output.TransferProcessedEventPublisher;
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
	public void publish(TransferEventPayload event) {
		TransferEvent protobufEvent = TransferEvent.newBuilder()
				.setEventId(event.eventId().toString())
				.setTransferId(event.transferId().toString())
				.setFromAccount(event.fromAccount())
				.setToAccount(event.toAccount())
				.setAmount(event.amount().toPlainString())
				.setCurrency(event.currency())
				.setStatus(event.status())
				.setCreatedAt(event.createdAt())
				.build();
		kafkaTemplate.send(TRANSFERS_PROCESSED_TOPIC, event.transferId().toString(), protobufEvent.toByteArray());
	}
}
