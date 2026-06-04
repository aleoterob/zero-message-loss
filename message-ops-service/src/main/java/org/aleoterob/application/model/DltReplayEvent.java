package org.aleoterob.application.model;

import java.time.Instant;

public record DltReplayEvent(
        String eventId,
        String key,
        byte[] payload,
        TransferEventDto dto,
        int replayAttempts,
        boolean replayed,
        Instant lastAttemptAt) {
    public DltReplayEvent withAttempt() {
        return new DltReplayEvent(eventId, key, payload, dto, replayAttempts + 1, replayed, Instant.now());
    }

    public DltReplayEvent replayed(TransferEventDto replayedDto) {
        return new DltReplayEvent(eventId, key, payload, replayedDto, replayAttempts, true, Instant.now());
    }
}
