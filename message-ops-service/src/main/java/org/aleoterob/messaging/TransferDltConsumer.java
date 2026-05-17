package org.aleoterob.messaging;

import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import org.aleoterob.DltReplayService;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TransferDltConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransferDltConsumer.class);

    private final DltReplayService dltReplayService;

    public TransferDltConsumer(DltReplayService dltReplayService) {
        this.dltReplayService = dltReplayService;
    }

    @Incoming("transfers-dlt")
    public CompletionStage<Void> consume(Message<byte[]> message) {
        // NOTE: Capture failed transfer events from DLT so the dashboard can show them and the replay service can recover them.
        String key = message.getMetadata(IncomingKafkaRecordMetadata.class)
                .map(IncomingKafkaRecordMetadata::getKey)
                .map(Object::toString)
                .orElse(null);
        try {
            dltReplayService.register(key, message.getPayload());
            log.warn("Published DLT transfer event to SSE stream and queued it for replay");
            return message.ack();
        } catch (Exception e) {
            return message.nack(e);
        }
    }
}
