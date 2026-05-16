package org.aleoterob;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessedEventMapper {
    private final ObjectMapper objectMapper;

    public ProcessedEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProcessedEventDto toDto(String debeziumEnvelope) {
        try {
            JsonNode root = objectMapper.readTree(debeziumEnvelope);
            JsonNode after = root.path("after");
            if (after.isMissingNode() || after.isNull()) {
                throw new IllegalArgumentException("Debezium processed event does not contain an after payload");
            }

            return new ProcessedEventDto(
                    text(after, "event_id"),
                    text(after, "transfer_id"),
                    text(after, "processed_at"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not map processed event envelope", e);
        }
    }

    private static String text(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            throw new IllegalArgumentException("Missing processed event field: " + fieldName);
        }
        return field.asText();
    }
}
