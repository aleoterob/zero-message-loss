package com.aleoterob.transfer_producer.infrastructure;

import com.aleoterob.transfer_producer.domain.Transfer;

public interface TransferRepository {
	Transfer save(Transfer transfer);
}
