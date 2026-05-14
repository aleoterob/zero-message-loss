package com.aleoterob.transfer_consumer.infrastructure;

import com.aleoterob.transfer_consumer.domain.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID>, ProcessedEventRepository {
}
