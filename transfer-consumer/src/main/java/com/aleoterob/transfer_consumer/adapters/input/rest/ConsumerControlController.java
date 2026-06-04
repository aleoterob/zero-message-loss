package com.aleoterob.transfer_consumer.adapters.input.rest;

import com.aleoterob.transfer_consumer.application.model.ConsumerStatus;
import com.aleoterob.transfer_consumer.application.ports.input.ConsumerControlUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumer")
public class ConsumerControlController {
	private final ConsumerControlUseCase consumerControlUseCase;

	public ConsumerControlController(ConsumerControlUseCase consumerControlUseCase) {
		this.consumerControlUseCase = consumerControlUseCase;
	}

	// NOTE: Exposes the consumer health flag used by message-ops-service before automatic replay.
	@GetMapping("/status")
	public ConsumerStatus status() {
		return consumerControlUseCase.status();
	}

	// NOTE: Turns processing failures on so transfer events can be retried and sent to DLT on purpose.
	@PostMapping("/fail-processing")
	public ConsumerStatus failProcessing() {
		consumerControlUseCase.enableFailProcessing();
		return consumerControlUseCase.status();
	}

	// NOTE: Restores normal processing so pending DLT events can be replayed safely.
	@PostMapping("/restore-processing")
	public ConsumerStatus restoreProcessing() {
		consumerControlUseCase.restoreProcessing();
		return consumerControlUseCase.status();
	}
}
