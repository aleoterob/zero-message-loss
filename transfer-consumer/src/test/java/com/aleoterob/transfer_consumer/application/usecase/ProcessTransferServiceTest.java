package com.aleoterob.transfer_consumer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer_consumer.application.model.TransferEventPayload;
import com.aleoterob.transfer_consumer.application.ports.output.ProcessedTransferRepository;
import com.aleoterob.transfer_consumer.application.ports.output.TransferProcessedEventPublisher;
import com.aleoterob.transfer_consumer.domain.model.ProcessedTransfer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProcessTransferServiceTest {

	@Test
	void storesEventIdWhenProcessingNewTransferEvent() throws Exception {
		InMemoryProcessedTransferRepository repository = new InMemoryProcessedTransferRepository();
		RecordingTransferProcessedEventPublisher publisher = new RecordingTransferProcessedEventPublisher();
		ConsumerControlService controlService = new ConsumerControlService();
		ProcessTransferService service = new ProcessTransferService(repository, controlService, publisher);
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();

		service.process(transferEvent(eventId, transferId));

		assertThat(repository.saved).singleElement()
				.satisfies(processedTransfer -> {
					assertThat(processedTransfer.getEventId()).isEqualTo(eventId);
					assertThat(processedTransfer.getTransferId()).isEqualTo(transferId);
					assertThat(processedTransfer.getFromAccount()).isEqualTo("ACC001");
					assertThat(processedTransfer.getToAccount()).isEqualTo("ACC002");
					assertThat(processedTransfer.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
					assertThat(processedTransfer.getCurrency()).isEqualTo("ARS");
					assertThat(processedTransfer.getStatus()).isEqualTo("PROCESSED");
					assertThat(processedTransfer.getProcessedAt()).isNotNull();
				});
		assertThat(publisher.published).singleElement()
				.satisfies(processedEvent -> {
					assertThat(processedEvent.eventId()).isEqualTo(eventId);
					assertThat(processedEvent.transferId()).isEqualTo(transferId);
					assertThat(processedEvent.fromAccount()).isEqualTo("ACC001");
					assertThat(processedEvent.toAccount()).isEqualTo("ACC002");
					assertThat(processedEvent.amount()).isEqualByComparingTo(new BigDecimal("1500.00"));
					assertThat(processedEvent.currency()).isEqualTo("ARS");
					assertThat(processedEvent.status()).isEqualTo("PROCESSED");
				});
	}

	@Test
	void ignoresDuplicateTransferEvent() throws Exception {
		InMemoryProcessedTransferRepository repository = new InMemoryProcessedTransferRepository();
		UUID eventId = UUID.randomUUID();
		UUID transferId = UUID.randomUUID();
		repository.save(ProcessedTransfer.create(transferEvent(eventId, transferId)));
		RecordingTransferProcessedEventPublisher publisher = new RecordingTransferProcessedEventPublisher();
		ConsumerControlService controlService = new ConsumerControlService();
		ProcessTransferService service = new ProcessTransferService(repository, controlService, publisher);

		service.process(transferEvent(eventId, transferId));

		assertThat(repository.saved).hasSize(1);
		assertThat(publisher.published).isEmpty();
	}

	@Test
	void throwsWhenFailureModeIsEnabledSoKafkaCanRetryAndDlt() {
		ConsumerControlService controlService = new ConsumerControlService();
		controlService.enableFailProcessing();
		ProcessTransferService service = new ProcessTransferService(
				new InMemoryProcessedTransferRepository(),
				controlService,
				new RecordingTransferProcessedEventPublisher());

		org.junit.jupiter.api.Assertions.assertThrows(
				IllegalStateException.class,
				() -> service.process(transferEvent(UUID.randomUUID(), UUID.randomUUID())));
	}

	private static TransferEventPayload transferEvent(UUID eventId, UUID transferId) {
		return new TransferEventPayload(
				eventId,
				transferId,
				"ACC001",
				"ACC002",
				new BigDecimal("1500.00"),
				"ARS",
				"PENDING",
				123456789L);
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

	private static final class RecordingTransferProcessedEventPublisher implements TransferProcessedEventPublisher {
		private final List<TransferEventPayload> published = new ArrayList<>();

		@Override
		public void publish(TransferEventPayload event) {
			published.add(event);
		}
	}
}
