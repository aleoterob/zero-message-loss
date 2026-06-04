package com.aleoterob.transfer_producer.application.ports.output;

import com.aleoterob.transfer_producer.domain.model.Transfer;

public interface TransferEventSerializer {
	byte[] serializeCreated(Transfer transfer);
}
