package com.aleoterob.transfer_consumer.application.ports.input;

import com.aleoterob.transfer_consumer.application.model.TransferEventPayload;

public interface ProcessTransferUseCase {
	void process(TransferEventPayload event);
}
