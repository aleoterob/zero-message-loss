package org.aleoterob.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aleoterob.application.model.ProcessedTransferDto;
import org.junit.jupiter.api.Test;

class ProcessedTransferMapperTest {

    @Test
    void mapsDebeziumAfterPayloadToProcessedTransferDto() {
        ProcessedTransferMapper mapper = new ProcessedTransferMapper(new ObjectMapper());
        String envelope = """
                {
                  "before": null,
                  "after": {
                    "event_id": "0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d",
                    "transfer_id": "2f48b775-5bb2-4f09-a728-dc9ac7c5aa12",
                    "from_account": "ACC001",
                    "to_account": "ACC002",
                    "amount": "1500.0000",
                    "currency": "ARS",
                    "status": "PENDING",
                    "processed_at": "2026-05-15T16:00:00Z"
                  },
                  "op": "c"
                }
                """;

        ProcessedTransferDto dto = mapper.toDto(envelope);

        assertEquals("0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d", dto.eventId());
        assertEquals("2f48b775-5bb2-4f09-a728-dc9ac7c5aa12", dto.transferId());
        assertEquals("ACC001", dto.fromAccount());
        assertEquals("ACC002", dto.toAccount());
        assertEquals("1500.0000", dto.amount());
        assertEquals("ARS", dto.currency());
        assertEquals("PENDING", dto.status());
        assertEquals("2026-05-15T16:00:00Z", dto.processedAt());
    }

    @Test
    void mapsHistoricalRowsCreatedBeforeTransferDetailsWereAdded() {
        ProcessedTransferMapper mapper = new ProcessedTransferMapper(new ObjectMapper());
        String envelope = """
                {
                  "before": null,
                  "after": {
                    "event_id": "0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d",
                    "transfer_id": "2f48b775-5bb2-4f09-a728-dc9ac7c5aa12",
                    "from_account": null,
                    "to_account": null,
                    "amount": null,
                    "currency": null,
                    "status": null,
                    "processed_at": "2026-05-15T16:00:00Z"
                  },
                  "op": "r"
                }
                """;

        ProcessedTransferDto dto = mapper.toDto(envelope);

        assertEquals("0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d", dto.eventId());
        assertEquals("2f48b775-5bb2-4f09-a728-dc9ac7c5aa12", dto.transferId());
        assertEquals("", dto.fromAccount());
        assertEquals("", dto.toAccount());
        assertEquals("", dto.amount());
        assertEquals("", dto.currency());
        assertEquals("", dto.status());
        assertEquals("2026-05-15T16:00:00Z", dto.processedAt());
    }

    @Test
    void ignoresDebeziumEnvelopeWithoutAfterPayload() {
        ProcessedTransferMapper mapper = new ProcessedTransferMapper(new ObjectMapper());
        String envelope = """
                {
                  "before": {
                    "event_id": "0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d",
                    "transfer_id": "2f48b775-5bb2-4f09-a728-dc9ac7c5aa12"
                  },
                  "after": null,
                  "op": "d"
                }
                """;

        assertTrue(mapper.toOptionalDto(envelope).isEmpty());
    }
}
