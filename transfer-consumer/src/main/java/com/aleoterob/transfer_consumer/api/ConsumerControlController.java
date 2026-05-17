package com.aleoterob.transfer_consumer.api;

import com.aleoterob.transfer_consumer.application.ConsumerControlService;
import com.aleoterob.transfer_consumer.application.ConsumerStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumer")
public class ConsumerControlController {
	private final ConsumerControlService consumerControlService;

	public ConsumerControlController(ConsumerControlService consumerControlService) {
		this.consumerControlService = consumerControlService;
	}

	// NOTE: Exposes the consumer health flag used by message-ops-service before automatic replay.
	@GetMapping("/status")
	public ConsumerStatus status() {
		return consumerControlService.status();
	}

	// NOTE: Turns processing failures on so transfer events can be retried and sent to DLT on purpose.
	@PostMapping("/fail-processing")
	public ConsumerStatus failProcessing() {
		consumerControlService.enableFailProcessing();
		return consumerControlService.status();
	}

	// NOTE: Restores normal processing so pending DLT events can be replayed safely.
	@PostMapping("/restore-processing")
	public ConsumerStatus restoreProcessing() {
		consumerControlService.restoreProcessing();
		return consumerControlService.status();
	}
}
