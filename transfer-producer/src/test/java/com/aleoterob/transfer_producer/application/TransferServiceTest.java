package com.aleoterob.transfer_producer.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_producer.api.dto.CreateTransferRequest;
import com.aleoterob.transfer_producer.domain.OutboxEvent;
import com.aleoterob.transfer_producer.domain.Transfer;
import com.aleoterob.transfer_producer.infrastructure.OutboxEventRepository;
import com.aleoterob.transfer_producer.infrastructure.TransferRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransferServiceTest {

	@Test
	void createsTransferAndOutboxEventWithProtobufPayload() throws Exception {
		InMemoryTransferRepository transferRepository = new InMemoryTransferRepository();
		InMemoryOutboxEventRepository outboxEventRepository = new InMemoryOutboxEventRepository();
		TransferService service = new TransferService(transferRepository, outboxEventRepository);

		Transfer transfer = service.create(new CreateTransferRequest(
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

		TransferEvent event = TransferEvent.parseFrom(outboxEvent.getPayload());
		assertThat(event.getEventId()).isNotBlank();
		assertThat(event.getTransferId()).isEqualTo(transfer.getId().toString());
		assertThat(event.getFromAccount()).isEqualTo("ACC001");
		assertThat(event.getToAccount()).isEqualTo("ACC002");
		assertThat(event.getAmount()).isEqualTo("1500.00");
		assertThat(event.getCurrency()).isEqualTo("ARS");
		assertThat(event.getStatus()).isEqualTo("PENDING");
		assertThat(event.getCreatedAt()).isPositive();
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
}
