package com.aleoterob.transfer_consumer.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_consumer.domain.ProcessedTransfer;
import com.aleoterob.transfer_consumer.infrastructure.ProcessedTransferRepository;
import com.google.protobuf.InvalidProtocolBufferException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

class TransferConsumerTest {

	@Test
	void storesEventIdWhenProcessingNewTransferEvent() throws Exception {
		InMemoryProcessedTransferRepository repository = new InMemoryProcessedTransferRepository();
		ConsumerControlService controlService = new ConsumerControlService();
		TransferConsumer consumer = new TransferConsumer(repository, controlService);
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		consumer.consume(transferEvent(eventId, transferId).toByteArray(), acknowledgment);

		assertThat(repository.saved).singleElement()
				.satisfies(processedTransfer -> {
					assertThat(processedTransfer.getEventId()).isEqualTo(eventId);
					assertThat(processedTransfer.getTransferId()).isEqualTo(transferId);
					assertThat(processedTransfer.getFromAccount()).isEqualTo("ACC001");
					assertThat(processedTransfer.getToAccount()).isEqualTo("ACC002");
					assertThat(processedTransfer.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
					assertThat(processedTransfer.getCurrency()).isEqualTo("ARS");
					assertThat(processedTransfer.getStatus()).isEqualTo("PENDING");
					assertThat(processedTransfer.getProcessedAt()).isNotNull();
				});
		assertThat(acknowledgment.acknowledged).isTrue();
	}

	@Test
	void ignoresDuplicateTransferEvent() throws Exception {
		InMemoryProcessedTransferRepository repository = new InMemoryProcessedTransferRepository();
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		repository.save(ProcessedTransfer.create(transferEvent(eventId, transferId)));
		ConsumerControlService controlService = new ConsumerControlService();
		TransferConsumer consumer = new TransferConsumer(repository, controlService);
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		consumer.consume(transferEvent(eventId, transferId).toByteArray(), acknowledgment);

		assertThat(repository.saved).hasSize(1);
		assertThat(acknowledgment.acknowledged).isTrue();
	}

	@Test
	void decodesBase64PayloadProducedByDebeziumOutboxRouter() throws Exception {
		InMemoryProcessedTransferRepository repository = new InMemoryProcessedTransferRepository();
		ConsumerControlService controlService = new ConsumerControlService();
		TransferConsumer consumer = new TransferConsumer(repository, controlService);
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();
		byte[] encodedPayload = Base64.getEncoder()
				.encode(transferEvent(eventId, transferId).toByteArray());

		consumer.consume(encodedPayload, acknowledgment);

		assertThat(repository.saved).singleElement()
				.satisfies(processedTransfer -> assertThat(processedTransfer.getEventId()).isEqualTo(eventId));
		assertThat(acknowledgment.acknowledged).isTrue();
	}

	@Test
	void throwsWhenPayloadCannotBeDeserializedSoKafkaCanRetryAndDlt() {
		ConsumerControlService controlService = new ConsumerControlService();
		TransferConsumer consumer = new TransferConsumer(new InMemoryProcessedTransferRepository(), controlService);
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		org.junit.jupiter.api.Assertions.assertThrows(
				InvalidProtocolBufferException.class,
				() -> consumer.consume(new byte[] {1, 2, 3}, acknowledgment));
		assertThat(acknowledgment.acknowledged).isFalse();
	}

	@Test
	void throwsWhenFailureModeIsEnabledSoKafkaCanRetryAndDlt() {
		ConsumerControlService controlService = new ConsumerControlService();
		controlService.enableFailProcessing();
		TransferConsumer consumer = new TransferConsumer(new InMemoryProcessedTransferRepository(), controlService);
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		org.junit.jupiter.api.Assertions.assertThrows(
				IllegalStateException.class,
				() -> consumer.consume(transferEvent(UUID.randomUUID(), UUID.randomUUID()).toByteArray(), acknowledgment));
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

	private static final class InMemoryProcessedTransferRepository implements ProcessedTransferRepository {
		private final List<ProcessedTransfer> saved = new ArrayList<>();

		@Override
		public boolean existsById(UUID eventId) {
			return saved.stream().anyMatch(processedTransfer -> processedTransfer.getEventId().equals(eventId));
		}

		@Override
		public ProcessedTransfer save(ProcessedTransfer processedTransfer) {
			saved.add(processedTransfer);
			return processedTransfer;
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
