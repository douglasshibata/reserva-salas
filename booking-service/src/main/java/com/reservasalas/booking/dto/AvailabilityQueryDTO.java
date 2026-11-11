package com.reservasalas.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityQueryDTO {
    private Long roomId;
    private String roomName;
    private Integer capacity;
    private String location;
    private LocalDateTime checkedStartTime;
    private LocalDateTime checkedEndTime;
    private Boolean isAvailable;
    private Integer conflictingReservationsCount;
}
