package org.aleoterob.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.aleoterob.application.model.TransferEventDto;
import org.junit.jupiter.api.Test;

class EventStreamBusTest {

    @Test
    void publishesCreatedEventsToCreatedStream() throws InterruptedException {
        EventStreamBus bus = new EventStreamBus();
        TransferEventDto event = dto(false);
        CountDownLatch latch = new CountDownLatch(1);
        List<TransferEventDto> received = new ArrayList<>();

        bus.createdStream().subscribe().with(receivedEvent -> {
            received.add(receivedEvent);
            latch.countDown();
        });
        bus.publishCreated(event);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of(event), received);
    }

    @Test
    void publishesDltEventsToDltStream() throws InterruptedException {
        EventStreamBus bus = new EventStreamBus();
        TransferEventDto event = dto(true);
        CountDownLatch latch = new CountDownLatch(1);
        List<TransferEventDto> received = new ArrayList<>();

        bus.dltStream().subscribe().with(receivedEvent -> {
            received.add(receivedEvent);
            latch.countDown();
        });
        bus.publishDlt(event);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of(event), received);
    }

    private static TransferEventDto dto(boolean isDlt) {
        return new TransferEventDto(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "ACC001",
                "ACC002",
                "1500.00",
                "ARS",
                "PENDING",
                123456789L,
                isDlt);
    }
}
