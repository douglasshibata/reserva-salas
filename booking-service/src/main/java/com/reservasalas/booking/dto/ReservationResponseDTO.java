package com.reservasalas.booking.dto;

import com.reservasalas.booking.model.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDTO {
    private Long id;
    private Long roomId;
    private String roomName;
    private String userEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private Boolean reminderSent;
    private LocalDateTime createdAt;
}
