package com.reservasalas.booking.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_consolidations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyConsolidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate consolidationDate;

    @Column(nullable = false)
    private Integer totalReservations;

    @Column(nullable = false)
    private Integer expiredReservations;

    @Column(nullable = false)
    private Double occupancyRate;
}
