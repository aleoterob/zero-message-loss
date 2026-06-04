package com.aleoterob.transfer_consumer.adapters.input.kafka;

import com.aleoterob.transfer_consumer.application.ports.input.ProcessTransferUseCase;
import com.aleoterob.transfer_consumer.application.usecase.ConsumerControlService;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class TransferConsumer {
	private final ProcessTransferUseCase processTransferUseCase;

	public TransferConsumer(ProcessTransferUseCase processTransferUseCase) {
		this.processTransferUseCase = processTransferUseCase;
	}

	@KafkaListener(id = ConsumerControlService.TRANSFER_LISTENER_ID, topics = "transfers.created", groupId = "transfer-consumer-group")
	public void consume(byte[] message, Acknowledgment acknowledgment) throws InvalidProtocolBufferException {
		processTransferUseCase.process(TransferEventPayloadParser.parse(message));
		acknowledgment.acknowledge();
	}
}
