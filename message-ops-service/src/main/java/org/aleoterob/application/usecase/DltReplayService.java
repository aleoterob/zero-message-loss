package org.aleoterob.application.usecase;

import com.google.protobuf.InvalidProtocolBufferException;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.aleoterob.adapters.output.http.ConsumerControlClient;
import org.aleoterob.application.mapper.TransferEventMapper;
import org.aleoterob.application.model.ConsumerStatusDto;
import org.aleoterob.application.model.DltReplayEvent;
import org.aleoterob.application.model.TransferEventDto;
import org.aleoterob.application.ports.output.DltReplayEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DltReplayService {
    private static final Logger log = LoggerFactory.getLogger(DltReplayService.class);

    private final EventStreamBus eventStreamBus;
    private final ConsumerControlClient consumerControlClient;
    private final Emitter<byte[]> replayEmitter;
    private final DltReplayEventRepository dltReplayEventRepository;

    public DltReplayService(
            EventStreamBus eventStreamBus,
            ConsumerControlClient consumerControlClient,
            @Channel("transfers-replay") Emitter<byte[]> replayEmitter,
            DltReplayEventRepository dltReplayEventRepository) {
        this.eventStreamBus = eventStreamBus;
        this.consumerControlClient = consumerControlClient;
        this.replayEmitter = replayEmitter;
        this.dltReplayEventRepository = dltReplayEventRepository;
    }

    public void register(String key, byte[] payload) throws InvalidProtocolBufferException {
        TransferEventDto dto = TransferEventMapper.toDto(payload, true, "DLT_PENDING", 0);
        dltReplayEventRepository.savePending(new DltReplayEvent(dto.eventId(), key, payload, dto, 0, false, null));
        eventStreamBus.publishDlt(dto);
        log.warn("Registered DLT event {} for automatic replay", dto.eventId());
    }

    @Scheduled(every = "3s", delayed = "10s")
    void replayPendingEvents() {
        var pendingEvents = dltReplayEventRepository.findPending();
        if (pendingEvents.isEmpty()) {
            return;
        }

        ConsumerStatusDto status = consumerControlClient.status();
        // NOTE: Replay only starts after the consumer reports that processing has been restored.
        if (!status.replayReady()) {
            return;
        }

        for (DltReplayEvent event : pendingEvents) {
            replay(event);
        }
    }

    private void replay(DltReplayEvent event) {
        DltReplayEvent attempted = event.withAttempt();
        dltReplayEventRepository.markReplayAttempt(attempted);

        OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String>builder()
                .withKey(event.key())
                .build();
        // NOTE: Republishes the original message to transfers.created, leaving normal consumer processing in charge again.
        replayEmitter.send(Message.of(event.payload()).addMetadata(metadata));

        TransferEventDto replayedDto = new TransferEventDto(
                event.dto().eventId(),
                event.dto().transferId(),
                event.dto().fromAccount(),
                event.dto().toAccount(),
                event.dto().amount(),
                event.dto().currency(),
                event.dto().status(),
                event.dto().createdAt(),
                true,
                "DLT_REPLAYED",
                attempted.replayAttempts());
        dltReplayEventRepository.markReplayed(attempted.replayed(replayedDto));
        eventStreamBus.publishDlt(replayedDto);
        log.info("Replayed DLT event {} at {}", event.eventId(), Instant.now());
    }

    public void confirmProcessed(String eventId) {
        dltReplayEventRepository.markConfirmed(eventId);
    }
}
