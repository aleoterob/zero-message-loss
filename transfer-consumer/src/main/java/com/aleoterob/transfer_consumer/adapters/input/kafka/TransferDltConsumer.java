package com.aleoterob.transfer_consumer.adapters.input.kafka;

import com.aleoterob.transfer_consumer.application.model.TransferEventPayload;
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
			TransferEventPayload event = TransferEventPayloadParser.parse(message);
			log.error("DLT event - transferId: {}, from: {}, to: {}, amount: {}",
					event.transferId(),
					event.fromAccount(),
					event.toAccount(),
					event.amount());
		} catch (Exception e) {
			log.error("Could not deserialize DLT message", e);
		}
	}
}
