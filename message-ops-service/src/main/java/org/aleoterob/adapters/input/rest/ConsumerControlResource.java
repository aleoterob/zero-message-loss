package org.aleoterob.adapters.input.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.aleoterob.adapters.output.http.ConsumerControlClient;
import org.aleoterob.application.model.ConsumerStatusDto;

@Path("/consumer")
public class ConsumerControlResource {
    private final ConsumerControlClient consumerControlClient;

    public ConsumerControlResource(ConsumerControlClient consumerControlClient) {
        this.consumerControlClient = consumerControlClient;
    }

    @GET
    @Path("/status")
    public ConsumerStatusDto status() {
        return consumerControlClient.status();
    }

    @POST
    @Path("/fail-processing")
    public ConsumerStatusDto failProcessing() {
        return consumerControlClient.failProcessing();
    }

    @POST
    @Path("/restore-processing")
    public ConsumerStatusDto restoreProcessing() {
        return consumerControlClient.restoreProcessing();
    }
}
