package com.aleoterob.transfer_producer.infrastructure;

import com.aleoterob.transfer_producer.domain.Transfer;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {
	Transfer save(Transfer transfer);

	Optional<Transfer> findById(UUID id);
}
