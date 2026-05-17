package com.aleoterob.transfer_consumer.application;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_consumer.domain.ProcessedTransfer;
import com.aleoterob.transfer_consumer.infrastructure.ProcessedTransferRepository;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransferConsumer {
	private static final Logger log = LoggerFactory.getLogger(TransferConsumer.class);

	private final ProcessedTransferRepository processedTransferRepository;
	private final ConsumerControlService consumerControlService;
	private final TransferProcessedEventPublisher transferProcessedEventPublisher;

	public TransferConsumer(ProcessedTransferRepository processedTransferRepository,
			ConsumerControlService consumerControlService,
			TransferProcessedEventPublisher transferProcessedEventPublisher) {
		this.processedTransferRepository = processedTransferRepository;
		this.consumerControlService = consumerControlService;
		this.transferProcessedEventPublisher = transferProcessedEventPublisher;
	}

	@KafkaListener(id = ConsumerControlService.TRANSFER_LISTENER_ID, topics = "transfers.created", groupId = "transfer-consumer-group")
	@Transactional
	public void consume(byte[] message, Acknowledgment acknowledgment) throws InvalidProtocolBufferException {
		// NOTE: Simulates a downstream outage so Spring Kafka can retry and route the event to DLT.
		if (consumerControlService.isFailProcessing()) {
			throw new IllegalStateException("Transfer consumer failure mode is enabled");
		}

		TransferEvent event = TransferEventPayload.parse(message);
		UUID eventId = UUID.fromString(event.getEventId());

		// NOTE: event_id is the idempotency key that prevents duplicate transfer processing.
		if (processedTransferRepository.existsById(eventId)) {
			log.warn("Duplicate event ignored: {}", eventId);
			acknowledgment.acknowledge();
			return;
		}

		log.info("Processing transfer: {} -> {} amount: {} {}",
				event.getFromAccount(),
				event.getToAccount(),
				event.getAmount(),
				event.getCurrency());

		// NOTE: Persist the consumer-side result, then publish the confirmation that lets the producer mark the transfer as processed.
		processedTransferRepository.save(ProcessedTransfer.create(event));
		transferProcessedEventPublisher.publish(toProcessedEvent(event));
		acknowledgment.acknowledge();
	}

	private static TransferEvent toProcessedEvent(TransferEvent event) {
		return event.toBuilder()
				.setStatus(ProcessedTransfer.PROCESSED_STATUS)
				.build();
	}
}
