package org.aleoterob;

import com.aleoterob.transfer.proto.TransferEvent;
import com.google.protobuf.InvalidProtocolBufferException;

public final class TransferEventMapper {
    private TransferEventMapper() {
    }

    public static TransferEventDto toDto(byte[] payload, boolean isDlt) throws InvalidProtocolBufferException {
        TransferEvent event = TransferEvent.parseFrom(payload);
        return new TransferEventDto(
                event.getEventId(),
                event.getTransferId(),
                event.getFromAccount(),
                event.getToAccount(),
                event.getAmount(),
                event.getCurrency(),
                event.getStatus(),
                event.getCreatedAt(),
                isDlt);
    }
}
