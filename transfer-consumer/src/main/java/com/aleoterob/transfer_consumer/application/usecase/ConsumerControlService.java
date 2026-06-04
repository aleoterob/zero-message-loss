package com.aleoterob.transfer_consumer.application.usecase;

import com.aleoterob.transfer_consumer.application.model.ConsumerStatus;
import com.aleoterob.transfer_consumer.application.ports.input.ConsumerControlUseCase;
import org.springframework.stereotype.Service;

@Service
public class ConsumerControlService implements ConsumerControlUseCase {
	public static final String TRANSFER_LISTENER_ID = "transfer-created-listener";

	private volatile boolean failProcessing;

	@Override
	public ConsumerStatus status() {
		return new ConsumerStatus(failProcessing);
	}

	public boolean isFailProcessing() {
		return failProcessing;
	}

	@Override
	public void enableFailProcessing() {
		failProcessing = true;
	}

	@Override
	public void restoreProcessing() {
		failProcessing = false;
	}
}
