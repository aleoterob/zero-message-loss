package org.aleoterob.adapters.input.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;
import org.aleoterob.application.mapper.ProcessedTransferMapper;
import org.aleoterob.application.model.ProcessedTransferDto;
import org.aleoterob.application.usecase.EventStreamBus;
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
            // NOTE: Debezium emits processed_transfers changes here, which confirms the consumer database write to the frontend.
            ProcessedTransferDto transfer = processedTransferMapper.toOptionalDto(message.getPayload())
                    .orElse(null);
            if (transfer == null) {
                log.debug("Ignored processed transfer message without an after payload");
                return message.ack();
            }
            eventStreamBus.publishProcessed(transfer);
            log.info("Published processed transfer {} to SSE stream", transfer.eventId());
            return message.ack();
        } catch (Exception e) {
            return message.nack(e);
        }
    }
}
