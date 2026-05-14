package com.aleoterob.transfer_producer.infrastructure;

import com.aleoterob.transfer_producer.domain.Transfer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTransferRepository extends JpaRepository<Transfer, UUID>, TransferRepository {
}
