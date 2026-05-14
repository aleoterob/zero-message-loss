package org.aleoterob.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.enterprise.context.ApplicationScoped;
import org.aleoterob.EventStreamBus;
import org.aleoterob.TransferEventMapper;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TransferCreatedConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransferCreatedConsumer.class);

    private final EventStreamBus eventStreamBus;

    public TransferCreatedConsumer(EventStreamBus eventStreamBus) {
        this.eventStreamBus = eventStreamBus;
    }

    @Incoming("transfers-created")
    public void consume(byte[] payload) throws InvalidProtocolBufferException {
        eventStreamBus.publishCreated(TransferEventMapper.toDto(payload, false));
        log.info("Published transfer event to SSE stream");
    }
}
