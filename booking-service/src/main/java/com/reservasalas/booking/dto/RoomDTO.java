package com.reservasalas.booking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private Long id;
    private String name;
    private Integer capacity;
    private String location;
    private String features;
    private Boolean active;
}
