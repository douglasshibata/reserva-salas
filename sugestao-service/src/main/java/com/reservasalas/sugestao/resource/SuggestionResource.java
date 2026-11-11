package com.reservasalas.sugestao.resource;

import com.reservasalas.sugestao.model.*;
import com.reservasalas.sugestao.service.SuggestionAlgorithmService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;

@Path("/api/sugestoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SuggestionResource {

    @Inject
    SuggestionAlgorithmService algorithmService;

    @POST
    public Response getSuggestions(SuggestionRequest request) {
        if (request == null) {
            request = new SuggestionRequest();
        }
        List<SuggestionResponse> suggestions = algorithmService.generateSuggestions(request);
        return Response.ok(suggestions).build();
    }

    @POST
    @Path("/aceitar")
    public Response acceptSuggestion(
            @QueryParam("roomId") Long roomId,
            @QueryParam("roomName") String roomName,
            @QueryParam("userEmail") String userEmail,
            @QueryParam("score") @DefaultValue("85.0") double score
    ) {
        SugestaoAceitaEvent event = algorithmService.acceptSuggestion(
                roomId != null ? roomId : 1L,
                roomName,
                userEmail,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                score
        );
        return Response.ok(event).build();
    }
}
