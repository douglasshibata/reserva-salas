package com.reservasalas.sugestao.model;

import java.time.LocalDateTime;

public class SuggestionRequest {
    public Integer minCapacity;
    public String requiredLocation;
    public LocalDateTime preferredStartTime;
    public LocalDateTime preferredEndTime;
    public Integer durationMinutes;

    public SuggestionRequest() {}

    public SuggestionRequest(Integer minCapacity, String requiredLocation, LocalDateTime preferredStartTime, LocalDateTime preferredEndTime, Integer durationMinutes) {
        this.minCapacity = minCapacity;
        this.requiredLocation = requiredLocation;
        this.preferredStartTime = preferredStartTime;
        this.preferredEndTime = preferredEndTime;
        this.durationMinutes = durationMinutes;
    }
}
