package org.aleoterob.application.model;

public record ConsumerStatusDto(boolean failProcessing) {
    public boolean replayReady() {
        return !failProcessing;
    }
}
