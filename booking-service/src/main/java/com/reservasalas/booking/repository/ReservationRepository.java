package com.reservasalas.booking.repository;

import com.reservasalas.booking.model.Reservation;
import com.reservasalas.booking.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId AND r.status = 'CONFIRMED' " +
           "AND r.startTime < :endTime AND r.endTime > :startTime")
    List<Reservation> findOverlappingReservations(@Param("roomId") Long roomId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    List<Reservation> findByStatusAndEndTimeBefore(ReservationStatus status, LocalDateTime now);

    List<Reservation> findByStatusAndReminderSentFalseAndStartTimeBetween(ReservationStatus status,
                                                                          LocalDateTime startWindow,
                                                                          LocalDateTime endWindow);

    List<Reservation> findByStartTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
