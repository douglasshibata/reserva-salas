package com.reservasalas.sugestao.model;

import java.time.LocalDateTime;

public class SugestaoAceitaEvent {
    public String eventId;
    public Long roomId;
    public String roomName;
    public String userEmail;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public double matchScore;
    public LocalDateTime acceptedAt;

    public SugestaoAceitaEvent() {}

    public SugestaoAceitaEvent(String eventId, Long roomId, String roomName, String userEmail, LocalDateTime startTime, LocalDateTime endTime, double matchScore, LocalDateTime acceptedAt) {
        this.eventId = eventId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.userEmail = userEmail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.matchScore = matchScore;
        this.acceptedAt = acceptedAt;
    }
}
