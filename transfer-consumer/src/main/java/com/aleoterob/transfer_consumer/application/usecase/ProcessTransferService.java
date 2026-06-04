package com.aleoterob.transfer_consumer.application.usecase;

import com.aleoterob.transfer_consumer.application.model.TransferEventPayload;
import com.aleoterob.transfer_consumer.application.ports.input.ProcessTransferUseCase;
import com.aleoterob.transfer_consumer.application.ports.output.ProcessedTransferRepository;
import com.aleoterob.transfer_consumer.application.ports.output.TransferProcessedEventPublisher;
import com.aleoterob.transfer_consumer.domain.model.ProcessedTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessTransferService implements ProcessTransferUseCase {
	private static final Logger log = LoggerFactory.getLogger(ProcessTransferService.class);

	private final ProcessedTransferRepository processedTransferRepository;
	private final ConsumerControlService consumerControlService;
	private final TransferProcessedEventPublisher transferProcessedEventPublisher;

	public ProcessTransferService(ProcessedTransferRepository processedTransferRepository,
			ConsumerControlService consumerControlService,
			TransferProcessedEventPublisher transferProcessedEventPublisher) {
		this.processedTransferRepository = processedTransferRepository;
		this.consumerControlService = consumerControlService;
		this.transferProcessedEventPublisher = transferProcessedEventPublisher;
	}

	@Override
	@Transactional
	public void process(TransferEventPayload event) {
		if (consumerControlService.isFailProcessing()) {
			throw new IllegalStateException("Transfer consumer failure mode is enabled");
		}

		if (processedTransferRepository.existsById(event.eventId())) {
			log.warn("Duplicate event ignored: {}", event.eventId());
			return;
		}

		log.info("Processing transfer: {} -> {} amount: {} {}",
				event.fromAccount(),
				event.toAccount(),
				event.amount(),
				event.currency());

		processedTransferRepository.save(ProcessedTransfer.create(event));
		transferProcessedEventPublisher.publish(event.markProcessed());
	}
}
