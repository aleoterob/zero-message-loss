package com.aleoterob.transfer_consumer.application;

import org.springframework.stereotype.Service;

@Service
public class ConsumerControlService {
	public static final String TRANSFER_LISTENER_ID = "transfer-created-listener";

	private volatile boolean failProcessing;

	public ConsumerStatus status() {
		return new ConsumerStatus(failProcessing);
	}

	public boolean isFailProcessing() {
		return failProcessing;
	}

	public void enableFailProcessing() {
		failProcessing = true;
	}

	public void restoreProcessing() {
		failProcessing = false;
	}
}
