package org.aleoterob.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;
import org.aleoterob.EventStreamBus;
import org.aleoterob.ProcessedTransferDto;
import org.aleoterob.ProcessedTransferMapper;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProcessedTransferConsumer {
    private static final Logger log = LoggerFactory.getLogger(ProcessedTransferConsumer.class);

    private final EventStreamBus eventStreamBus;
    private final ProcessedTransferMapper processedTransferMapper;

    public ProcessedTransferConsumer(EventStreamBus eventStreamBus, ProcessedTransferMapper processedTransferMapper) {
        this.eventStreamBus = eventStreamBus;
        this.processedTransferMapper = processedTransferMapper;
    }

    @Incoming("processed-transfers")
    public CompletionStage<Void> consume(Message<String> message) {
        try {
            ProcessedTransferDto transfer = processedTransferMapper.toDto(message.getPayload());
            eventStreamBus.publishProcessed(transfer);
            log.info("Published processed transfer {} to SSE stream", transfer.eventId());
            return message.ack();
        } catch (Exception e) {
            return message.nack(e);
        }
    }
}
