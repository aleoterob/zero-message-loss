package org.aleoterob;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

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
    @Path("/pause")
    public ConsumerStatusDto pause() {
        return consumerControlClient.pause();
    }

    @POST
    @Path("/resume")
    public ConsumerStatusDto resume() {
        return consumerControlClient.resume();
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
