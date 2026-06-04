package com.aleoterob.transfer_consumer.adapters.input.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_consumer.application.model.TransferEventPayload;
import com.aleoterob.transfer_consumer.application.ports.input.ProcessTransferUseCase;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

class TransferConsumerTest {
	@Test
	void parsesMessageDelegatesAndAcknowledges() throws Exception {
		RecordingProcessTransferUseCase useCase = new RecordingProcessTransferUseCase();
		TransferConsumer consumer = new TransferConsumer(useCase);
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		consumer.consume(transferEvent(eventId, transferId).toByteArray(), acknowledgment);

		assertThat(useCase.processed).singleElement()
				.satisfies(event -> {
					assertThat(event.eventId()).isEqualTo(eventId);
					assertThat(event.transferId()).isEqualTo(transferId);
				});
		assertThat(acknowledgment.acknowledged).isTrue();
	}

	@Test
	void decodesBase64PayloadProducedByDebeziumOutboxRouter() throws Exception {
		RecordingProcessTransferUseCase useCase = new RecordingProcessTransferUseCase();
		TransferConsumer consumer = new TransferConsumer(useCase);
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();
		byte[] encodedPayload = Base64.getEncoder().encode(transferEvent(eventId, transferId).toByteArray());

		consumer.consume(encodedPayload, acknowledgment);

		assertThat(useCase.processed).singleElement()
				.satisfies(event -> assertThat(event.eventId()).isEqualTo(eventId));
		assertThat(acknowledgment.acknowledged).isTrue();
	}

	@Test
	void throwsWhenPayloadCannotBeDeserializedSoKafkaCanRetryAndDlt() {
		TransferConsumer consumer = new TransferConsumer(new RecordingProcessTransferUseCase());
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		org.junit.jupiter.api.Assertions.assertThrows(
				InvalidProtocolBufferException.class,
				() -> consumer.consume(new byte[] { 1, 2, 3 }, acknowledgment));
		assertThat(acknowledgment.acknowledged).isFalse();
	}

	private static TransferEvent transferEvent(UUID eventId, UUID transferId) {
		return TransferEvent.newBuilder()
				.setEventId(eventId.toString())
				.setTransferId(transferId.toString())
				.setFromAccount("ACC001")
				.setToAccount("ACC002")
				.setAmount("1500.00")
				.setCurrency("ARS")
				.setStatus("PENDING")
				.setCreatedAt(123456789L)
				.build();
	}

	private static final class RecordingProcessTransferUseCase implements ProcessTransferUseCase {
		private final List<TransferEventPayload> processed = new ArrayList<>();

		@Override
		public void process(TransferEventPayload event) {
			processed.add(event);
		}
	}

	private static final class RecordingAcknowledgment implements Acknowledgment {
		private boolean acknowledged;

		@Override
		public void acknowledge() {
			acknowledged = true;
		}
	}
}
