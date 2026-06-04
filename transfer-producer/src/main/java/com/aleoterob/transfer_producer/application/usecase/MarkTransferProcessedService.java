package com.aleoterob.transfer_producer.application.usecase;

import com.aleoterob.transfer_producer.application.ports.input.MarkTransferProcessedUseCase;
import com.aleoterob.transfer_producer.application.ports.output.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MarkTransferProcessedService implements MarkTransferProcessedUseCase {
	private final TransferRepository transferRepository;

	public MarkTransferProcessedService(TransferRepository transferRepository) {
		this.transferRepository = transferRepository;
	}

	@Override
	@Transactional
	public void markProcessed(UUID transferId) {
		transferRepository.findById(transferId)
				.ifPresent(transfer -> {
					transfer.markProcessed();
					transferRepository.save(transfer);
				});
	}
}
