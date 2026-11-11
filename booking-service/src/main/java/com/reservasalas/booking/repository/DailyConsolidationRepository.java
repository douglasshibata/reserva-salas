package com.reservasalas.booking.repository;

import com.reservasalas.booking.model.DailyConsolidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyConsolidationRepository extends JpaRepository<DailyConsolidation, Long> {

    Optional<DailyConsolidation> findByConsolidationDate(LocalDate consolidationDate);
}
