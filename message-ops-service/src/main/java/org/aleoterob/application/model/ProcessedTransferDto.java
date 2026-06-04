package org.aleoterob.application.model;

public record ProcessedTransferDto(
        String eventId,
        String transferId,
        String fromAccount,
        String toAccount,
        String amount,
        String currency,
        String status,
        String processedAt) {
}
