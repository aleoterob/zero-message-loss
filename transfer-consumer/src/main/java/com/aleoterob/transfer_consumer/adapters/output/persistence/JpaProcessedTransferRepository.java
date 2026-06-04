package com.aleoterob.transfer_consumer.adapters.output.persistence;

import com.aleoterob.transfer_consumer.application.ports.output.ProcessedTransferRepository;
import com.aleoterob.transfer_consumer.domain.model.ProcessedTransfer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProcessedTransferRepository extends JpaRepository<ProcessedTransfer, UUID>, ProcessedTransferRepository {
}
