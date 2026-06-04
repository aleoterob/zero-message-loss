package com.aleoterob.transfer_producer.application.ports.output;

import com.aleoterob.transfer_producer.domain.model.Transfer;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {
	Transfer save(Transfer transfer);

	Optional<Transfer> findById(UUID id);
}
