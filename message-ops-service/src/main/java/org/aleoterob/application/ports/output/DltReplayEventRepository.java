package org.aleoterob.application.ports.output;

import java.util.List;
import org.aleoterob.application.model.DltReplayEvent;

public interface DltReplayEventRepository {
    void savePending(DltReplayEvent event);

    List<DltReplayEvent> findPending();

    void markReplayAttempt(DltReplayEvent event);

    void markReplayed(DltReplayEvent event);

    void markConfirmed(String eventId);
}
