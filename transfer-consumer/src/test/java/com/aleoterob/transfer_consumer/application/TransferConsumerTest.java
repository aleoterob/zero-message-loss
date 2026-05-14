package com.aleoterob.transfer_consumer.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_consumer.domain.ProcessedEvent;
import com.aleoterob.transfer_consumer.infrastructure.ProcessedEventRepository;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

class TransferConsumerTest {

	@Test
	void storesEventIdWhenProcessingNewTransferEvent() throws Exception {
		InMemoryProcessedEventRepository repository = new InMemoryProcessedEventRepository();
		ConsumerControlService controlService = new ConsumerControlService(null);
		TransferConsumer consumer = new TransferConsumer(repository, controlService);
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		consumer.consume(transferEvent(eventId, transferId).toByteArray(), acknowledgment);

		assertThat(repository.saved).singleElement()
				.satisfies(processedEvent -> {
					assertThat(processedEvent.getEventId()).isEqualTo(eventId);
					assertThat(processedEvent.getTransferId()).isEqualTo(transferId);
					assertThat(processedEvent.getProcessedAt()).isNotNull();
				});
		assertThat(acknowledgment.acknowledged).isTrue();
	}

	@Test
	void ignoresDuplicateTransferEvent() throws Exception {
		InMemoryProcessedEventRepository repository = new InMemoryProcessedEventRepository();
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		repository.save(ProcessedEvent.create(eventId, transferId));
		ConsumerControlService controlService = new ConsumerControlService(null);
		TransferConsumer consumer = new TransferConsumer(repository, controlService);
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		consumer.consume(transferEvent(eventId, transferId).toByteArray(), acknowledgment);

		assertThat(repository.saved).hasSize(1);
		assertThat(acknowledgment.acknowledged).isTrue();
	}

	@Test
	void decodesBase64PayloadProducedByDebeziumOutboxRouter() throws Exception {
		InMemoryProcessedEventRepository repository = new InMemoryProcessedEventRepository();
		ConsumerControlService controlService = new ConsumerControlService(null);
		TransferConsumer consumer = new TransferConsumer(repository, controlService);
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();
		byte[] encodedPayload = Base64.getEncoder()
				.encode(transferEvent(eventId, transferId).toByteArray());

		consumer.consume(encodedPayload, acknowledgment);

		assertThat(repository.saved).singleElement()
				.satisfies(processedEvent -> assertThat(processedEvent.getEventId()).isEqualTo(eventId));
		assertThat(acknowledgment.acknowledged).isTrue();
	}

	@Test
	void throwsWhenPayloadCannotBeDeserializedSoKafkaCanRetryAndDlt() {
		ConsumerControlService controlService = new ConsumerControlService(null);
		TransferConsumer consumer = new TransferConsumer(new InMemoryProcessedEventRepository(), controlService);
		RecordingAcknowledgment acknowledgment = new RecordingAcknowledgment();

		org.junit.jupiter.api.Assertions.assertThrows(
				InvalidProtocolBufferException.class,
				() -> consumer.consume(new byte[] {1, 2, 3}, acknowledgment));
		assertThat(acknowledgment.acknowledged).isFalse();
	}

	@Test
	void throwsWhenFailureModeIsEnabledSoKafkaCanRetryAndDlt() {
		ConsumerControlService controlService = new ConsumerControlService(null);
		controlService.enableFailProcessing();
		TransferConsumer consumer = new TransferConsumer(new InMemoryProcessedEventRepository(), controlService);
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

	private static final class InMemoryProcessedEventRepository implements ProcessedEventRepository {
		private final List<ProcessedEvent> saved = new ArrayList<>();

		@Override
		public boolean existsById(UUID eventId) {
			return saved.stream().anyMatch(processedEvent -> processedEvent.getEventId().equals(eventId));
		}

		@Override
		public ProcessedEvent save(ProcessedEvent processedEvent) {
			saved.add(processedEvent);
			return processedEvent;
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
