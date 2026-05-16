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
    private final List<MultiEmitter<? super ProcessedEventDto>> processedEmitters = new CopyOnWriteArrayList<>();

    public Multi<TransferEventDto> createdStream() {
        return stream(createdEmitters);
    }

    public Multi<TransferEventDto> dltStream() {
        return stream(dltEmitters);
    }

    public Multi<ProcessedEventDto> processedStream() {
        return stream(processedEmitters);
    }

    public void publishCreated(TransferEventDto event) {
        publish(createdEmitters, event);
    }

    public void publishDlt(TransferEventDto event) {
        publish(dltEmitters, event);
    }

    public void publishProcessed(ProcessedEventDto event) {
        publish(processedEmitters, event);
    }

    private <T> Multi<T> stream(List<MultiEmitter<? super T>> emitters) {
        return Multi.createFrom().emitter(emitter -> {
            emitters.add(emitter);
            emitter.onTermination(() -> emitters.remove(emitter));
        });
    }

    private <T> void publish(List<MultiEmitter<? super T>> emitters, T event) {
        for (MultiEmitter<? super T> emitter : emitters) {
            emitter.emit(event);
        }
    }
}
