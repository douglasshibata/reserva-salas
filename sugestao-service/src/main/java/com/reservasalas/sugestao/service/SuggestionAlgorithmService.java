package com.reservasalas.sugestao.service;

import com.reservasalas.sugestao.client.BookingServiceClient;
import com.reservasalas.sugestao.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class SuggestionAlgorithmService {

    @Inject
    @RestClient
    BookingServiceClient bookingServiceClient;

    public List<SuggestionResponse> generateSuggestions(SuggestionRequest req) {
        LocalDateTime start = req.preferredStartTime != null ? req.preferredStartTime : LocalDateTime.now().plusHours(1);
        LocalDateTime end = req.preferredEndTime != null ? req.preferredEndTime : start.plusHours(1);
        int targetCapacity = req.minCapacity != null ? req.minCapacity : 1;

        List<AvailabilityQueryDTO> availabilities;
        try {
            availabilities = bookingServiceClient.checkAvailability(null, targetCapacity, start.toString(), end.toString());
        } catch (Exception e) {
            availabilities = getFallbackAvailabilities(targetCapacity, start, end);
        }

        List<SuggestionResponse> suggestions = new ArrayList<>();

        for (AvailabilityQueryDTO item : availabilities) {
            double score = 0.0;
            StringBuilder reasonBuilder = new StringBuilder();

            if (Boolean.TRUE.equals(item.isAvailable)) {
                score += 50.0;
                reasonBuilder.append("Totalmente disponível no horário desejado (+50pts). ");
            } else {
                score -= 30.0;
                reasonBuilder.append("Possui ").append(item.conflictingReservationsCount).append(" conflito(s) de agendamento (-30pts). ");
            }

            int roomCapacity = item.capacity != null ? item.capacity : 0;
            if (roomCapacity >= targetCapacity) {
                int extraSeats = roomCapacity - targetCapacity;
                double capScore = Math.max(5.0, 30.0 - (extraSeats * 1.5));
                score += capScore;
                reasonBuilder.append(String.format("Capacidade adequada (%d assentos, +%.1fpts). ", roomCapacity, capScore));
            } else {
                score -= 40.0;
                reasonBuilder.append(String.format("Capacidade insuficiente (%d assentos, -40pts). ", roomCapacity));
            }

            if (req.requiredLocation != null && !req.requiredLocation.isBlank() && item.location != null) {
                if (item.location.toLowerCase().contains(req.requiredLocation.toLowerCase())) {
                    score += 20.0;
                    reasonBuilder.append("Localização correspondente (+20pts). ");
                }
            } else {
                score += 10.0;
            }

            suggestions.add(new SuggestionResponse(
                    item.roomId,
                    item.roomName,
                    item.capacity,
                    item.location,
                    start,
                    end,
                    Math.max(0.0, Math.round(score * 10.0) / 10.0),
                    reasonBuilder.toString().trim()
            ));
        }

        suggestions.sort(Comparator.comparingDouble((SuggestionResponse s) -> s.score).reversed());
        return suggestions;
    }

    public SugestaoAceitaEvent acceptSuggestion(Long roomId, String roomName, String userEmail, LocalDateTime startTime, LocalDateTime endTime, double score) {
        return new SugestaoAceitaEvent(
                UUID.randomUUID().toString(),
                roomId,
                roomName != null ? roomName : "Sala Agendada",
                userEmail != null ? userEmail : "usuario@empresa.com",
                startTime != null ? startTime : LocalDateTime.now(),
                endTime != null ? endTime : LocalDateTime.now().plusHours(1),
                score,
                LocalDateTime.now()
        );
    }

    private List<AvailabilityQueryDTO> getFallbackAvailabilities(int targetCapacity, LocalDateTime start, LocalDateTime end) {
        List<AvailabilityQueryDTO> list = new ArrayList<>();

        AvailabilityQueryDTO r1 = new AvailabilityQueryDTO();
        r1.roomId = 1L;
        r1.roomName = "Sala Inovação (Fallback)";
        r1.capacity = Math.max(10, targetCapacity);
        r1.location = "Bloco A";
        r1.checkedStartTime = start;
        r1.checkedEndTime = end;
        r1.isAvailable = true;
        r1.conflictingReservationsCount = 0;
        list.add(r1);

        AvailabilityQueryDTO r2 = new AvailabilityQueryDTO();
        r2.roomId = 2L;
        r2.roomName = "Auditório Principal (Fallback)";
        r2.capacity = Math.max(50, targetCapacity + 10);
        r2.location = "Bloco B";
        r2.checkedStartTime = start;
        r2.checkedEndTime = end;
        r2.isAvailable = true;
        r2.conflictingReservationsCount = 0;
        list.add(r2);

        return list;
    }
}
