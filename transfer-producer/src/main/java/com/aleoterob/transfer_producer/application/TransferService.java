package com.aleoterob.transfer_producer.application;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_producer.api.dto.CreateTransferRequest;
import com.aleoterob.transfer_producer.domain.OutboxEvent;
import com.aleoterob.transfer_producer.domain.Transfer;
import com.aleoterob.transfer_producer.infrastructure.OutboxEventRepository;
import com.aleoterob.transfer_producer.infrastructure.TransferRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransferService {
	private static final String TRANSFER_STATUS_PENDING = "PENDING";
	private static final String AGGREGATE_TYPE_TRANSFER = "Transfer";
	private static final String EVENT_TYPE_TRANSFER_CREATED = "TransferCreated";

	private final TransferRepository transferRepository;
	private final OutboxEventRepository outboxEventRepository;

	public TransferService(TransferRepository transferRepository, OutboxEventRepository outboxEventRepository) {
		this.transferRepository = transferRepository;
		this.outboxEventRepository = outboxEventRepository;
	}

	public Transfer create(CreateTransferRequest request) {
		Transfer transfer = Transfer.create(
				request.fromAccount(),
				request.toAccount(),
				request.amount(),
				request.currency(),
				TRANSFER_STATUS_PENDING);
		Transfer savedTransfer = transferRepository.save(transfer);

		TransferEvent event = TransferEvent.newBuilder()
				.setEventId(UUID.randomUUID().toString())
				.setTransferId(savedTransfer.getId().toString())
				.setFromAccount(savedTransfer.getFromAccount())
				.setToAccount(savedTransfer.getToAccount())
				.setAmount(savedTransfer.getAmount().toPlainString())
				.setCurrency(savedTransfer.getCurrency())
				.setStatus(savedTransfer.getStatus())
				.setCreatedAt(Instant.now().toEpochMilli())
				.build();

		outboxEventRepository.save(OutboxEvent.create(
				savedTransfer.getId(),
				AGGREGATE_TYPE_TRANSFER,
				EVENT_TYPE_TRANSFER_CREATED,
				event.toByteArray()));

		return savedTransfer;
	}
}
