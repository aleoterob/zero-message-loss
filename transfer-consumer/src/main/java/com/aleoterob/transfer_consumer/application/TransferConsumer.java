package com.aleoterob.transfer_consumer.application;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_consumer.domain.ProcessedEvent;
import com.aleoterob.transfer_consumer.infrastructure.ProcessedEventRepository;
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

	private final ProcessedEventRepository processedEventRepository;

	public TransferConsumer(ProcessedEventRepository processedEventRepository) {
		this.processedEventRepository = processedEventRepository;
	}

	@KafkaListener(topics = "transfers.created", groupId = "transfer-consumer-group")
	@Transactional
	public void consume(byte[] message, Acknowledgment acknowledgment) throws InvalidProtocolBufferException {
		TransferEvent event = TransferEvent.parseFrom(message);
		UUID eventId = UUID.fromString(event.getEventId());

		if (processedEventRepository.existsById(eventId)) {
			log.warn("Duplicate event ignored: {}", eventId);
			acknowledgment.acknowledge();
			return;
		}

		log.info("Processing transfer: {} -> {} amount: {} {}",
				event.getFromAccount(),
				event.getToAccount(),
				event.getAmount(),
				event.getCurrency());

		processedEventRepository.save(ProcessedEvent.create(eventId, UUID.fromString(event.getTransferId())));
		acknowledgment.acknowledge();
	}
}
