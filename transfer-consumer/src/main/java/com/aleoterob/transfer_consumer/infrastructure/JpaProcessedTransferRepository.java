package com.aleoterob.transfer_consumer.infrastructure;

import com.aleoterob.transfer_consumer.domain.ProcessedTransfer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProcessedTransferRepository extends JpaRepository<ProcessedTransfer, UUID>, ProcessedTransferRepository {
}
