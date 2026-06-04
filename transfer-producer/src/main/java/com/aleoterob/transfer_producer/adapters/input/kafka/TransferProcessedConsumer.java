package com.aleoterob.transfer_producer.adapters.input.kafka;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_producer.application.ports.input.MarkTransferProcessedUseCase;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferProcessedConsumer {
	private final MarkTransferProcessedUseCase markTransferProcessedUseCase;

	public TransferProcessedConsumer(MarkTransferProcessedUseCase markTransferProcessedUseCase) {
		this.markTransferProcessedUseCase = markTransferProcessedUseCase;
	}

	@KafkaListener(topics = "transfers.processed", groupId = "transfer-producer-processed-group")
	public void consume(byte[] message) throws InvalidProtocolBufferException {
		TransferEvent event = TransferEvent.parseFrom(message);
		UUID transferId = UUID.fromString(event.getTransferId());

		markTransferProcessedUseCase.markProcessed(transferId);
	}
}
