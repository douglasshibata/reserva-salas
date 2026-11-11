package com.reservasalas.sugestao.model;

import java.time.LocalDateTime;

public class SuggestionResponse {
    public Long roomId;
    public String roomName;
    public Integer capacity;
    public String location;
    public LocalDateTime suggestedStartTime;
    public LocalDateTime suggestedEndTime;
    public double score;
    public String reason;

    public SuggestionResponse() {}

    public SuggestionResponse(Long roomId, String roomName, Integer capacity, String location, LocalDateTime suggestedStartTime, LocalDateTime suggestedEndTime, double score, String reason) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
        this.location = location;
        this.suggestedStartTime = suggestedStartTime;
        this.suggestedEndTime = suggestedEndTime;
        this.score = score;
        this.reason = reason;
    }
}
