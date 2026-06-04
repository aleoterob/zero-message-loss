package com.aleoterob.transfer_producer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer_producer.application.command.CreateTransferCommand;
import com.aleoterob.transfer_producer.application.ports.output.OutboxEventRepository;
import com.aleoterob.transfer_producer.application.ports.output.TransferEventSerializer;
import com.aleoterob.transfer_producer.application.ports.output.TransferRepository;
import com.aleoterob.transfer_producer.domain.model.OutboxEvent;
import com.aleoterob.transfer_producer.domain.model.Transfer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateTransferServiceTest {

	@Test
	void createsTransferAndOutboxEventPayload() {
		InMemoryTransferRepository transferRepository = new InMemoryTransferRepository();
		InMemoryOutboxEventRepository outboxEventRepository = new InMemoryOutboxEventRepository();
		StubTransferEventSerializer transferEventSerializer = new StubTransferEventSerializer();
		CreateTransferService service = new CreateTransferService(
				transferRepository,
				outboxEventRepository,
				transferEventSerializer);

		Transfer transfer = service.create(new CreateTransferCommand(
				"ACC001",
				"ACC002",
				new BigDecimal("1500.00"),
				"ARS"));

		assertThat(transfer.getId()).isNotNull();
		assertThat(transfer.getStatus()).isEqualTo("PENDING");
		assertThat(transferRepository.saved).containsExactly(transfer);
		assertThat(outboxEventRepository.saved).hasSize(1);

		OutboxEvent outboxEvent = outboxEventRepository.saved.getFirst();
		assertThat(outboxEvent.getAggregateId()).isEqualTo(transfer.getId());
		assertThat(outboxEvent.getAggregateType()).isEqualTo("Transfer");
		assertThat(outboxEvent.getEventType()).isEqualTo("TransferCreated");
		assertThat(outboxEvent.getPayload()).containsExactly(1, 2, 3);
		assertThat(transferEventSerializer.serialized).containsExactly(transfer);
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

	private static final class InMemoryOutboxEventRepository implements OutboxEventRepository {
		private final List<OutboxEvent> saved = new ArrayList<>();

		@Override
		public OutboxEvent save(OutboxEvent outboxEvent) {
			saved.add(outboxEvent);
			return outboxEvent;
		}
	}

	private static final class StubTransferEventSerializer implements TransferEventSerializer {
		private final List<Transfer> serialized = new ArrayList<>();

		@Override
		public byte[] serializeCreated(Transfer transfer) {
			serialized.add(transfer);
			return new byte[] { 1, 2, 3 };
		}
	}
}
