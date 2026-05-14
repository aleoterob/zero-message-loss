package org.aleoterob;

import java.time.Instant;

record DltReplayEvent(
        String eventId,
        String key,
        byte[] payload,
        TransferEventDto dto,
        int replayAttempts,
        boolean replayed,
        Instant lastAttemptAt) {
    DltReplayEvent withAttempt() {
        return new DltReplayEvent(eventId, key, payload, dto, replayAttempts + 1, replayed, Instant.now());
    }

    DltReplayEvent replayed(TransferEventDto replayedDto) {
        return new DltReplayEvent(eventId, key, payload, replayedDto, replayAttempts, true, Instant.now());
    }
}
