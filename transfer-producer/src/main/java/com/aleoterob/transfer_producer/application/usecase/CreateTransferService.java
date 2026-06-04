package com.aleoterob.transfer_producer.application.usecase;

import com.aleoterob.transfer_producer.application.command.CreateTransferCommand;
import com.aleoterob.transfer_producer.application.ports.input.CreateTransferUseCase;
import com.aleoterob.transfer_producer.application.ports.output.OutboxEventRepository;
import com.aleoterob.transfer_producer.application.ports.output.TransferEventSerializer;
import com.aleoterob.transfer_producer.application.ports.output.TransferRepository;
import com.aleoterob.transfer_producer.domain.model.OutboxEvent;
import com.aleoterob.transfer_producer.domain.model.Transfer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateTransferService implements CreateTransferUseCase {
	private static final String TRANSFER_STATUS_PENDING = "PENDING";
	private static final String AGGREGATE_TYPE_TRANSFER = "Transfer";
	private static final String EVENT_TYPE_TRANSFER_CREATED = "TransferCreated";

	private final TransferRepository transferRepository;
	private final OutboxEventRepository outboxEventRepository;
	private final TransferEventSerializer transferEventSerializer;

	public CreateTransferService(
			TransferRepository transferRepository,
			OutboxEventRepository outboxEventRepository,
			TransferEventSerializer transferEventSerializer) {
		this.transferRepository = transferRepository;
		this.outboxEventRepository = outboxEventRepository;
		this.transferEventSerializer = transferEventSerializer;
	}

	@Override
	// NOTE: Starts the transactional outbox flow: persist the transfer and its event in the same database transaction.
	public Transfer create(CreateTransferCommand command) {
		Transfer transfer = Transfer.create(
				command.fromAccount(),
				command.toAccount(),
				command.amount(),
				command.currency(),
				TRANSFER_STATUS_PENDING);
		// NOTE: Insert transfer in transfer table of transfer-producer-db.
		Transfer savedTransfer = transferRepository.save(transfer);
		// NOTE: Insert outbox event in outbox_events table of transfer-producer-db.
		outboxEventRepository.save(OutboxEvent.create(
				savedTransfer.getId(),
				AGGREGATE_TYPE_TRANSFER,
				EVENT_TYPE_TRANSFER_CREATED,
				transferEventSerializer.serializeCreated(savedTransfer)));

		return savedTransfer;
	}
}
