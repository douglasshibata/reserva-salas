package com.reservasalas.sugestao.client;

import com.reservasalas.sugestao.model.AvailabilityQueryDTO;
import com.reservasalas.sugestao.model.RoomDTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/api/salas")
@RegisterRestClient(configKey = "booking-service-api")
public interface BookingServiceClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RoomDTO> getAllActiveRooms();

    @GET
    @Path("/disponibilidade")
    @Produces(MediaType.APPLICATION_JSON)
    List<AvailabilityQueryDTO> checkAvailability(
            @QueryParam("roomId") Long roomId,
            @QueryParam("minCapacity") Integer minCapacity,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime
    );
}
