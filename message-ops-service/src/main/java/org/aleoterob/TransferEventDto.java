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
        boolean isDlt,
        String deliveryState,
        int replayAttempts) {
    public TransferEventDto(
            String eventId,
            String transferId,
            String fromAccount,
            String toAccount,
            String amount,
            String currency,
            String status,
            long createdAt,
            boolean isDlt) {
        this(
                eventId,
                transferId,
                fromAccount,
                toAccount,
                amount,
                currency,
                status,
                createdAt,
                isDlt,
                isDlt ? "DLT_PENDING" : "LIVE",
                0);
    }
}
