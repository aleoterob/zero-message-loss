package com.aleoterob.transfer_consumer.application;

import com.aleoterob.transfer.proto.TransferEvent;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

class TransferDltConsumerTest {
	private Logger logger;
	private Level originalLevel;

	@BeforeEach
	void muteDltLogger() {
		logger = (Logger) LoggerFactory.getLogger(TransferDltConsumer.class);
		originalLevel = logger.getLevel();
		logger.setLevel(Level.OFF);
	}

	@AfterEach
	void restoreDltLogger() {
		logger.setLevel(originalLevel);
	}

	@Test
	void acceptsDeserializableDltPayload() {
		TransferDltConsumer consumer = new TransferDltConsumer();
		TransferEvent event = TransferEvent.newBuilder()
				.setEventId(UUID.randomUUID().toString())
				.setTransferId(UUID.randomUUID().toString())
				.setFromAccount("ACC001")
				.setToAccount("ACC002")
				.setAmount("1500.00")
				.setCurrency("ARS")
				.setStatus("PENDING")
				.setCreatedAt(123456789L)
				.build();

		consumer.handleDlt(event.toByteArray());
	}

	@Test
	void acceptsInvalidDltPayloadForLoggingOnly() {
		TransferDltConsumer consumer = new TransferDltConsumer();

		consumer.handleDlt(new byte[] {1, 2, 3});
	}
}
