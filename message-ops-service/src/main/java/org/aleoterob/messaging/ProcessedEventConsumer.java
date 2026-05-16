package org.aleoterob.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;
import org.aleoterob.EventStreamBus;
import org.aleoterob.ProcessedEventDto;
import org.aleoterob.ProcessedEventMapper;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProcessedEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(ProcessedEventConsumer.class);

    private final EventStreamBus eventStreamBus;
    private final ProcessedEventMapper processedEventMapper;

    public ProcessedEventConsumer(EventStreamBus eventStreamBus, ProcessedEventMapper processedEventMapper) {
        this.eventStreamBus = eventStreamBus;
        this.processedEventMapper = processedEventMapper;
    }

    @Incoming("processed-events")
    public CompletionStage<Void> consume(Message<String> message) {
        try {
            ProcessedEventDto event = processedEventMapper.toDto(message.getPayload());
            eventStreamBus.publishProcessed(event);
            log.info("Published processed event {} to SSE stream", event.eventId());
            return message.ack();
        } catch (Exception e) {
            return message.nack(e);
        }
    }
}
