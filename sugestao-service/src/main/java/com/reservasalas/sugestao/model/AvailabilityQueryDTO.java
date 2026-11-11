package com.reservasalas.sugestao.model;

import java.time.LocalDateTime;

public class AvailabilityQueryDTO {
    public Long roomId;
    public String roomName;
    public Integer capacity;
    public String location;
    public LocalDateTime checkedStartTime;
    public LocalDateTime checkedEndTime;
    public Boolean isAvailable;
    public Integer conflictingReservationsCount;

    public AvailabilityQueryDTO() {}
}
