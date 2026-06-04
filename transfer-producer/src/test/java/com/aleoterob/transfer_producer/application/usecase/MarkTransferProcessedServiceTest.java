package com.aleoterob.transfer_producer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer_producer.application.ports.output.TransferRepository;
import com.aleoterob.transfer_producer.domain.model.Transfer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MarkTransferProcessedServiceTest {
	@Test
	void marksTransferAsProcessed() {
		InMemoryTransferRepository repository = new InMemoryTransferRepository();
		Transfer transfer = repository.save(Transfer.create(
				"ACC001",
				"ACC002",
				new BigDecimal("1500.00"),
				"ARS",
				"PENDING"));
		MarkTransferProcessedService service = new MarkTransferProcessedService(repository);

		service.markProcessed(transfer.getId());

		assertThat(transfer.getStatus()).isEqualTo("PROCESSED");
		assertThat(repository.saved).containsExactly(transfer, transfer);
	}

	@Test
	void ignoresUnknownTransfer() {
		InMemoryTransferRepository repository = new InMemoryTransferRepository();
		MarkTransferProcessedService service = new MarkTransferProcessedService(repository);

		service.markProcessed(UUID.randomUUID());

		assertThat(repository.saved).isEmpty();
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
