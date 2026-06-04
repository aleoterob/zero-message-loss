package com.aleoterob.transfer_consumer.application.ports.input;

import com.aleoterob.transfer_consumer.application.model.ConsumerStatus;

public interface ConsumerControlUseCase {
	ConsumerStatus status();

	void enableFailProcessing();

	void restoreProcessing();
}
