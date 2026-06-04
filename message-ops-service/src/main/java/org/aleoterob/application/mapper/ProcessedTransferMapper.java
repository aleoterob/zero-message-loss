package org.aleoterob.application.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.aleoterob.application.model.ProcessedTransferDto;

@ApplicationScoped
public class ProcessedTransferMapper {
    private final ObjectMapper objectMapper;

    public ProcessedTransferMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProcessedTransferDto toDto(String debeziumEnvelope) {
        return toOptionalDto(debeziumEnvelope)
                .orElseThrow(() -> new IllegalArgumentException("Debezium processed transfer does not contain an after payload"));
    }

    public Optional<ProcessedTransferDto> toOptionalDto(String debeziumEnvelope) {
        try {
            JsonNode root = objectMapper.readTree(debeziumEnvelope);
            JsonNode after = root.path("after");
            if (after.isMissingNode() || after.isNull()) {
                return Optional.empty();
            }

            return Optional.of(new ProcessedTransferDto(
                    text(after, "event_id"),
                    text(after, "transfer_id"),
                    optionalText(after, "from_account"),
                    optionalText(after, "to_account"),
                    optionalText(after, "amount"),
                    optionalText(after, "currency"),
                    optionalText(after, "status"),
                    text(after, "processed_at")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not map processed transfer envelope", e);
        }
    }

    private static String text(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            throw new IllegalArgumentException("Missing processed transfer field: " + fieldName);
        }
        return field.asText();
    }

    private static String optionalText(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return "";
        }
        return field.asText();
    }
}
