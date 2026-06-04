package org.aleoterob.adapters.input.rest;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.aleoterob.application.model.ProcessedTransferDto;
import org.aleoterob.application.model.TransferEventDto;
import org.aleoterob.application.usecase.EventStreamBus;

@Path("/events")
@Produces(MediaType.SERVER_SENT_EVENTS)
public class EventStreamResource {
    private final EventStreamBus eventStreamBus;

    public EventStreamResource(EventStreamBus eventStreamBus) {
        this.eventStreamBus = eventStreamBus;
    }

    @GET
    @Path("/stream")
    public Multi<TransferEventDto> streamCreated() {
        return eventStreamBus.createdStream();
    }

    @GET
    @Path("/dlt")
    public Multi<TransferEventDto> streamDlt() {
        return eventStreamBus.dltStream();
    }

    @GET
    @Path("/processed")
    public Multi<ProcessedTransferDto> streamProcessed() {
        return eventStreamBus.processedStream();
    }
}
