package org.aleoterob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ProcessedEventMapperTest {

    @Test
    void mapsDebeziumAfterPayloadToProcessedEventDto() {
        ProcessedEventMapper mapper = new ProcessedEventMapper(new ObjectMapper());
        String envelope = """
                {
                  "before": null,
                  "after": {
                    "event_id": "0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d",
                    "transfer_id": "2f48b775-5bb2-4f09-a728-dc9ac7c5aa12",
                    "processed_at": "2026-05-15T16:00:00Z"
                  },
                  "op": "c"
                }
                """;

        ProcessedEventDto dto = mapper.toDto(envelope);

        assertEquals("0e7e2d4d-f44f-4a6d-8b74-4f1d4d5b3f8d", dto.eventId());
        assertEquals("2f48b775-5bb2-4f09-a728-dc9ac7c5aa12", dto.transferId());
        assertEquals("2026-05-15T16:00:00Z", dto.processedAt());
    }
}
