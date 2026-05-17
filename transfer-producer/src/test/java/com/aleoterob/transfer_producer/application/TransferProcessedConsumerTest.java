package com.aleoterob.transfer_producer.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_producer.domain.Transfer;
import com.aleoterob.transfer_producer.infrastructure.TransferRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransferProcessedConsumerTest {

	@Test
	void marksTransferAsProcessedWhenProcessedEventArrives() throws Exception {
		InMemoryTransferRepository repository = new InMemoryTransferRepository();
		Transfer transfer = repository.save(Transfer.create(
				"ACC001",
				"ACC002",
				new BigDecimal("1500.00"),
				"ARS",
				"PENDING"));
		TransferProcessedConsumer consumer = new TransferProcessedConsumer(repository);

		consumer.consume(processedEvent(transfer.getId()).toByteArray());

		assertThat(transfer.getStatus()).isEqualTo("PROCESSED");
		assertThat(repository.saved).containsExactly(transfer, transfer);
	}

	@Test
	void ignoresProcessedEventForUnknownTransfer() throws Exception {
		InMemoryTransferRepository repository = new InMemoryTransferRepository();
		TransferProcessedConsumer consumer = new TransferProcessedConsumer(repository);

		consumer.consume(processedEvent(UUID.randomUUID()).toByteArray());

		assertThat(repository.saved).isEmpty();
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

	private static final class InMemoryTransferRepository implements TransferRepository {
		private final List<Transfer> saved = new ArrayList<>();

		@Override
		public Transfer save(Transfer transfer) {
			saved.add(transfer);
			return transfer;
		}

		@Override
		public Optional<Transfer> findById(UUID id) {
			return saved.stream()
					.filter(transfer -> transfer.getId().equals(id))
					.findFirst();
		}
	}
}
