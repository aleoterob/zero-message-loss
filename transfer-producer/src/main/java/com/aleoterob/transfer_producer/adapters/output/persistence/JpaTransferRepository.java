package com.aleoterob.transfer_producer.adapters.output.persistence;

import com.aleoterob.transfer_producer.application.ports.output.TransferRepository;
import com.aleoterob.transfer_producer.domain.model.Transfer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTransferRepository extends JpaRepository<Transfer, UUID>, TransferRepository {
}
