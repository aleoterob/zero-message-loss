package com.aleoterob.transfer_producer.adapters.input.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_producer.application.ports.input.MarkTransferProcessedUseCase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransferProcessedConsumerTest {

	@Test
	void delegatesTransferIdWhenProcessedEventArrives() throws Exception {
		StubMarkTransferProcessedUseCase useCase = new StubMarkTransferProcessedUseCase();
		TransferProcessedConsumer consumer = new TransferProcessedConsumer(useCase);
		UUID transferId = UUID.randomUUID();

		consumer.consume(processedEvent(transferId).toByteArray());

		assertThat(useCase.processedTransferIds).containsExactly(transferId);
	}

	private static TransferEvent processedEvent(UUID transferId) {
		return TransferEvent.newBuilder()
				.setEventId(UUID.randomUUID().toString())
				.setTransferId(transferId.toString())
				.setFromAccount("ACC001")
				.setToAccount("ACC002")
				.setAmount("1500.00")
				.setCurrency("ARS")
				.setStatus("PROCESSED")
				.setCreatedAt(123456789L)
				.build();
	}

	private static final class StubMarkTransferProcessedUseCase implements MarkTransferProcessedUseCase {
		private final List<UUID> processedTransferIds = new ArrayList<>();

		@Override
		public void markProcessed(UUID transferId) {
			processedTransferIds.add(transferId);
		}
	}
}
