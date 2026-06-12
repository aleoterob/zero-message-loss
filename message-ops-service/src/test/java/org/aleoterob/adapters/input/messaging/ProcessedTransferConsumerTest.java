package org.aleoterob.adapters.input.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.aleoterob.application.mapper.ProcessedTransferMapper;
import org.aleoterob.application.model.DltReplayEvent;
import org.aleoterob.application.ports.output.DltReplayEventRepository;
import org.aleoterob.application.usecase.DltReplayService;
import org.aleoterob.application.usecase.EventStreamBus;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

class ProcessedTransferConsumerTest {
    @Test
    void acknowledgesDebeziumEnvelopeWithoutAfterPayload() {
        FakeDltReplayEventRepository repository = new FakeDltReplayEventRepository();
        ProcessedTransferConsumer consumer = new ProcessedTransferConsumer(
                new EventStreamBus(),
                new ProcessedTransferMapper(new ObjectMapper()),
                new DltReplayService(new EventStreamBus(), null, null, repository));
        AtomicBoolean acknowledged = new AtomicBoolean();
        AtomicBoolean nacked = new AtomicBoolean();
        String envelope = """
                {
                  "before": {
                    "event_id": "0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d",
                    "transfer_id": "2f48b775-5bb2-4f09-a728-dc9ac7c5aa12"
                  },
                  "after": null,
                  "op": "d"
                }
                """;
        Message<String> message = Message.of(
                envelope,
                () -> {
                    acknowledged.set(true);
                    return CompletableFuture.completedFuture(null);
                },
                reason -> {
                    nacked.set(true);
                    return CompletableFuture.completedFuture(null);
                });

        consumer.consume(message).toCompletableFuture().join();

        assertTrue(acknowledged.get());
        assertFalse(nacked.get());
    }

    @Test
    void confirmsDltReplayWhenProcessedTransferArrives() {
        FakeDltReplayEventRepository repository = new FakeDltReplayEventRepository();
        ProcessedTransferConsumer consumer = new ProcessedTransferConsumer(
                new EventStreamBus(),
                new ProcessedTransferMapper(new ObjectMapper()),
                new DltReplayService(new EventStreamBus(), null, null, repository));
        AtomicBoolean acknowledged = new AtomicBoolean();
        String envelope = """
                {
                  "after": {
                    "event_id": "0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d",
                    "transfer_id": "2f48b775-5bb2-4f09-a728-dc9ac7c5aa12",
                    "from_account": "ACC001",
                    "to_account": "ACC002",
                    "amount": "1500.00",
                    "currency": "ARS",
                    "status": "PROCESSED",
                    "processed_at": "2026-06-12T00:00:00Z"
                  },
                  "op": "c"
                }
                """;
        Message<String> message = Message.of(
                envelope,
                () -> {
                    acknowledged.set(true);
                    return CompletableFuture.completedFuture(null);
                });

        consumer.consume(message).toCompletableFuture().join();

        assertTrue(acknowledged.get());
        assertTrue(repository.confirmedEventIds.contains("0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d"));
    }

    private static final class FakeDltReplayEventRepository implements DltReplayEventRepository {
        private final List<String> confirmedEventIds = new ArrayList<>();

        @Override
        public void savePending(DltReplayEvent event) {
        }

        @Override
        public List<DltReplayEvent> findPending() {
            return List.of();
        }

        @Override
        public void markReplayAttempt(DltReplayEvent event) {
        }

        @Override
        public void markReplayed(DltReplayEvent event) {
        }

        @Override
        public void markConfirmed(String eventId) {
            confirmedEventIds.add(eventId);
        }
    }
}
