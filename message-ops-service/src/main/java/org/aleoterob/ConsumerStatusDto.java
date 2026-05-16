package org.aleoterob;

public record ConsumerStatusDto(boolean failProcessing) {
    public boolean replayReady() {
        return !failProcessing;
    }
}
