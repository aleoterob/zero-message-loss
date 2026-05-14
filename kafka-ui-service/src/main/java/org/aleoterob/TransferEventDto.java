package org.aleoterob;

public record TransferEventDto(
        String eventId,
        String transferId,
        String fromAccount,
        String toAccount,
        String amount,
        String currency,
        String status,
        long createdAt,
        boolean isDlt) {
}
