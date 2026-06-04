package com.aleoterob.transfer_producer.adapters.output.persistence;

import com.aleoterob.transfer_producer.application.ports.output.OutboxEventRepository;
import com.aleoterob.transfer_producer.domain.model.OutboxEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, UUID>, OutboxEventRepository {
}
