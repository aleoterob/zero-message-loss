package org.aleoterob.adapters.input.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.aleoterob.application.mapper.ProcessedTransferMapper;
import org.aleoterob.application.usecase.EventStreamBus;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

class ProcessedTransferConsumerTest {
    @Test
    void acknowledgesDebeziumEnvelopeWithoutAfterPayload() {
        ProcessedTransferConsumer consumer = new ProcessedTransferConsumer(
                new EventStreamBus(),
                new ProcessedTransferMapper(new ObjectMapper()));
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
}
