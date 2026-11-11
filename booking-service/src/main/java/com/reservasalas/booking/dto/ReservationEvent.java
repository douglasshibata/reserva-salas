package com.reservasalas.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEvent {
    private String eventId;
    private Long reservationId;
    private Long roomId;
    private String roomName;
    private String userEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String eventType;
    private LocalDateTime timestamp;
}
