package org.aleoterob.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aleoterob.transfer.proto.TransferEvent;
import java.util.ArrayList;
import java.util.List;
import org.aleoterob.application.model.DltReplayEvent;
import org.aleoterob.application.ports.output.DltReplayEventRepository;
import org.junit.jupiter.api.Test;

class DltReplayServiceTest {

    @Test
    void registersDltEventAsPendingInRepository() throws Exception {
        FakeDltReplayEventRepository repository = new FakeDltReplayEventRepository();
        DltReplayService service = new DltReplayService(new EventStreamBus(), null, null, repository);

        service.register("transfer-key", transferEvent().toByteArray());

        assertEquals(1, repository.pendingEvents.size());
        DltReplayEvent event = repository.pendingEvents.getFirst();
        assertEquals("event-1", event.eventId());
        assertEquals("transfer-key", event.key());
        assertEquals("DLT_PENDING", event.dto().deliveryState());
    }

    private static TransferEvent transferEvent() {
        return TransferEvent.newBuilder()
                .setEventId("event-1")
                .setTransferId("transfer-1")
                .setFromAccount("ACC001")
                .setToAccount("ACC002")
                .setAmount("1500.00")
                .setCurrency("ARS")
                .setStatus("PENDING")
                .setCreatedAt(123456789L)
                .build();
    }

    private static final class FakeDltReplayEventRepository implements DltReplayEventRepository {
        private final List<DltReplayEvent> pendingEvents = new ArrayList<>();

        @Override
        public void savePending(DltReplayEvent event) {
            pendingEvents.add(event);
        }

        @Override
        public List<DltReplayEvent> findPending() {
            return pendingEvents;
        }

        @Override
        public void markReplayAttempt(DltReplayEvent event) {
        }

        @Override
        public void markReplayed(DltReplayEvent event) {
        }

        @Override
        public void markConfirmed(String eventId) {
        }
    }
}
