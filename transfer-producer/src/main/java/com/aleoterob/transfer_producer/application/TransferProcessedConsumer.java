package com.aleoterob.transfer_producer.application;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_producer.infrastructure.TransferRepository;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransferProcessedConsumer {
	private final TransferRepository transferRepository;

	public TransferProcessedConsumer(TransferRepository transferRepository) {
		this.transferRepository = transferRepository;
	}

	@KafkaListener(topics = "transfers.processed", groupId = "transfer-producer-processed-group")
	@Transactional
	public void consume(byte[] message) throws InvalidProtocolBufferException {
		TransferEvent event = TransferEvent.parseFrom(message);
		UUID transferId = UUID.fromString(event.getTransferId());

		transferRepository.findById(transferId)
				.ifPresent(transfer -> {
					transfer.markProcessed();
					transferRepository.save(transfer);
				});
	}
}
