package org.aleoterob;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class EventStreamBus {
    private final List<MultiEmitter<? super TransferEventDto>> createdEmitters = new CopyOnWriteArrayList<>();
    private final List<MultiEmitter<? super TransferEventDto>> dltEmitters = new CopyOnWriteArrayList<>();

    public Multi<TransferEventDto> createdStream() {
        return stream(createdEmitters);
    }

    public Multi<TransferEventDto> dltStream() {
        return stream(dltEmitters);
    }

    public void publishCreated(TransferEventDto event) {
        publish(createdEmitters, event);
    }

    public void publishDlt(TransferEventDto event) {
        publish(dltEmitters, event);
    }

    private Multi<TransferEventDto> stream(List<MultiEmitter<? super TransferEventDto>> emitters) {
        return Multi.createFrom().emitter(emitter -> {
            emitters.add(emitter);
            emitter.onTermination(() -> emitters.remove(emitter));
        });
    }

    private void publish(List<MultiEmitter<? super TransferEventDto>> emitters, TransferEventDto event) {
        for (MultiEmitter<? super TransferEventDto> emitter : emitters) {
            emitter.emit(event);
        }
    }
}
