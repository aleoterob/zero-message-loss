package org.aleoterob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aleoterob.transfer.proto.TransferEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransferEventMapperTest {

    @Test
    void mapsProtobufEventToCreatedDto() throws Exception {
        TransferEvent event = transferEvent();

        TransferEventDto dto = TransferEventMapper.toDto(event.toByteArray(), false);

        assertEquals(event.getEventId(), dto.eventId());
        assertEquals(event.getTransferId(), dto.transferId());
        assertEquals("ACC001", dto.fromAccount());
        assertEquals("ACC002", dto.toAccount());
        assertEquals("1500.00", dto.amount());
        assertEquals("ARS", dto.currency());
        assertEquals("PENDING", dto.status());
        assertEquals(123456789L, dto.createdAt());
        assertFalse(dto.isDlt());
    }

    @Test
    void mapsProtobufEventToDltDto() throws Exception {
        TransferEvent event = transferEvent();

        TransferEventDto dto = TransferEventMapper.toDto(event.toByteArray(), true);

        assertTrue(dto.isDlt());
    }

    private static TransferEvent transferEvent() {
        return TransferEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTransferId(UUID.randomUUID().toString())
                .setFromAccount("ACC001")
                .setToAccount("ACC002")
                .setAmount("1500.00")
                .setCurrency("ARS")
                .setStatus("PENDING")
                .setCreatedAt(123456789L)
                .build();
    }
}
