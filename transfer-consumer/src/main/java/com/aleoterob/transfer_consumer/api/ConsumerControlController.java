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

	@GetMapping("/status")
	public ConsumerStatus status() {
		return consumerControlService.status();
	}

	@PostMapping("/pause")
	public ConsumerStatus pause() {
		consumerControlService.pause();
		return consumerControlService.status();
	}

	@PostMapping("/resume")
	public ConsumerStatus resume() {
		consumerControlService.resume();
		return consumerControlService.status();
	}

	@PostMapping("/fail-processing")
	public ConsumerStatus failProcessing() {
		consumerControlService.enableFailProcessing();
		return consumerControlService.status();
	}

	@PostMapping("/restore-processing")
	public ConsumerStatus restoreProcessing() {
		consumerControlService.restoreProcessing();
		return consumerControlService.status();
	}
}
