package org.aleoterob.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.enterprise.context.ApplicationScoped;
import org.aleoterob.EventStreamBus;
import org.aleoterob.TransferEventMapper;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TransferDltConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransferDltConsumer.class);

    private final EventStreamBus eventStreamBus;

    public TransferDltConsumer(EventStreamBus eventStreamBus) {
        this.eventStreamBus = eventStreamBus;
    }

    @Incoming("transfers-dlt")
    public void consume(byte[] payload) throws InvalidProtocolBufferException {
        eventStreamBus.publishDlt(TransferEventMapper.toDto(payload, true));
        log.warn("Published DLT transfer event to SSE stream");
    }
}
