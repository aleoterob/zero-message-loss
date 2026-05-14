package org.aleoterob;

public record ConsumerStatusDto(boolean paused, boolean failProcessing) {
    public boolean replayReady() {
        return !paused && !failProcessing;
    }
}
