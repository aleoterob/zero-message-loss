package org.aleoterob;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConsumerControlClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String transferConsumerUrl;

    public ConsumerControlClient(
            ObjectMapper objectMapper,
            @ConfigProperty(name = "transfer-consumer.url") String transferConsumerUrl) {
        this.objectMapper = objectMapper;
        this.transferConsumerUrl = transferConsumerUrl;
    }

    public ConsumerStatusDto status() {
        HttpRequest request = HttpRequest.newBuilder(uri("/consumer/status")).GET().build();
        return send(request);
    }

    public ConsumerStatusDto pause() {
        return post("/consumer/pause");
    }

    public ConsumerStatusDto resume() {
        return post("/consumer/resume");
    }

    public ConsumerStatusDto failProcessing() {
        return post("/consumer/fail-processing");
    }

    public ConsumerStatusDto restoreProcessing() {
        return post("/consumer/restore-processing");
    }

    private ConsumerStatusDto post(String path) {
        HttpRequest request = HttpRequest.newBuilder(uri(path))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return send(request);
    }

    private ConsumerStatusDto send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Consumer control request failed with status " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), ConsumerStatusDto.class);
        } catch (IOException e) {
            throw new IllegalStateException("Consumer control request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Consumer control request interrupted", e);
        }
    }

    private URI uri(String path) {
        return URI.create(transferConsumerUrl + path);
    }
}
