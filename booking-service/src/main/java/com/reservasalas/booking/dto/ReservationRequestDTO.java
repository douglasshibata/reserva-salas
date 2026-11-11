package com.reservasalas.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequestDTO {
    private Long roomId;
    private String userEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
