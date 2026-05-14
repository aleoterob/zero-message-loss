package com.aleoterob.transfer_producer.infrastructure;

import com.aleoterob.transfer_producer.domain.OutboxEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, UUID>, OutboxEventRepository {
}
