package com.aleoterob.transfer_producer.application.ports.input;

import com.aleoterob.transfer_producer.application.command.CreateTransferCommand;
import com.aleoterob.transfer_producer.domain.model.Transfer;

public interface CreateTransferUseCase {
	Transfer create(CreateTransferCommand command);
}
