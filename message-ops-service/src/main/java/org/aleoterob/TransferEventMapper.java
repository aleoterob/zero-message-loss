package org.aleoterob;

import com.aleoterob.transfer.proto.TransferEvent;
import com.google.protobuf.InvalidProtocolBufferException;

public final class TransferEventMapper {
    private TransferEventMapper() {
    }

    public static TransferEventDto toDto(byte[] payload, boolean isDlt) throws InvalidProtocolBufferException {
        return toDto(payload, isDlt, isDlt ? "DLT_PENDING" : "LIVE", 0);
    }

    public static TransferEventDto toDto(
            byte[] payload,
            boolean isDlt,
            String deliveryState,
            int replayAttempts) throws InvalidProtocolBufferException {
        TransferEvent event = TransferEventPayload.parse(payload);
        return new TransferEventDto(
                event.getEventId(),
                event.getTransferId(),
                event.getFromAccount(),
                event.getToAccount(),
                event.getAmount(),
                event.getCurrency(),
                event.getStatus(),
                event.getCreatedAt(),
                isDlt,
                deliveryState,
                replayAttempts);
    }
}
