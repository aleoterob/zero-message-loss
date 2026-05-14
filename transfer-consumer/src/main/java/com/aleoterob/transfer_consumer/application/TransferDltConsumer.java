package com.aleoterob.transfer_consumer.application;

import com.aleoterob.transfer.proto.TransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferDltConsumer {
	private static final Logger log = LoggerFactory.getLogger(TransferDltConsumer.class);

	@KafkaListener(topics = "transfers.created.DLT", groupId = "transfer-dlt-group")
	public void handleDlt(byte[] message) {
		log.error("Message landed in DLT. Raw bytes length: {}", message.length);
		try {
			TransferEvent event = TransferEvent.parseFrom(message);
			log.error("DLT event - transferId: {}, from: {}, to: {}, amount: {}",
					event.getTransferId(),
					event.getFromAccount(),
					event.getToAccount(),
					event.getAmount());
		} catch (Exception e) {
			log.error("Could not deserialize DLT message", e);
		}
	}
}
