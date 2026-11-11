package com.reservasalas.booking.service;

import com.reservasalas.booking.dto.*;
import com.reservasalas.booking.exception.InvalidReservationException;
import com.reservasalas.booking.exception.ReservationConflictException;
import com.reservasalas.booking.exception.ResourceNotFoundException;
import com.reservasalas.booking.model.Reservation;
import com.reservasalas.booking.model.ReservationStatus;
import com.reservasalas.booking.model.Room;
import com.reservasalas.booking.repository.ReservationRepository;
import com.reservasalas.booking.repository.RoomRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final CachedAvailabilityService cachedAvailabilityService;
    private final Scheduler jpaScheduler;
    private final Sinks.Many<ReservationEvent> reservationEventSink;
    private final MeterRegistry meterRegistry;

    public Flux<AvailabilityQueryDTO> checkAvailability(Long roomId, Integer minCapacity, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now();
        LocalDateTime end = endTime != null ? endTime : start.plusHours(1);

        return Mono.fromCallable(() -> cachedAvailabilityService.getAvailabilityCached(roomId, minCapacity, start, end))
                .subscribeOn(jpaScheduler)
                .flatMapMany(Flux::fromIterable);
    }

    @CacheEvict(value = "availability", allEntries = true)
    public Mono<ReservationResponseDTO> createReservation(ReservationRequestDTO dto) {
        return Mono.fromCallable(() -> {
                    if (dto.getStartTime() == null || dto.getEndTime() == null || !dto.getEndTime().isAfter(dto.getStartTime())) {
                        throw new InvalidReservationException("Horário de início e término inválidos.");
                    }

                    Room room = roomRepository.findById(dto.getRoomId())
                            .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com o ID: " + dto.getRoomId()));

                    List<Reservation> overlaps = reservationRepository.findOverlappingReservations(
                            dto.getRoomId(), dto.getStartTime(), dto.getEndTime());

                    if (!overlaps.isEmpty()) {
                        meterRegistry.counter("reservas.conflitos", "roomId", String.valueOf(dto.getRoomId())).increment();
                        throw new ReservationConflictException("A sala " + room.getName() + " já possui reserva no horário solicitado.");
                    }

                    Reservation reservation = Reservation.builder()
                            .roomId(room.getId())
                            .userEmail(dto.getUserEmail())
                            .startTime(dto.getStartTime())
                            .endTime(dto.getEndTime())
                            .status(ReservationStatus.CONFIRMED)
                            .reminderSent(false)
                            .build();

                    Reservation saved = reservationRepository.save(reservation);
                    meterRegistry.counter("reservas.criadas", "roomId", String.valueOf(dto.getRoomId())).increment();

                    ReservationEvent event = ReservationEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .reservationId(saved.getId())
                            .roomId(room.getId())
                            .roomName(room.getName())
                            .userEmail(saved.getUserEmail())
                            .startTime(saved.getStartTime())
                            .endTime(saved.getEndTime())
                            .eventType("CONFIRMED")
                            .timestamp(LocalDateTime.now())
                            .build();

                    reservationEventSink.tryEmitNext(event);

                    return mapToResponseDTO(saved, room.getName());
                })
                .subscribeOn(jpaScheduler);
    }

    @CacheEvict(value = "availability", allEntries = true)
    public Mono<Void> cancelReservation(Long id) {
        return Mono.fromCallable(() -> {
                    Reservation reservation = reservationRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada com ID: " + id));

                    reservation.setStatus(ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation);

                    Room room = roomRepository.findById(reservation.getRoomId()).orElse(null);
                    String roomName = room != null ? room.getName() : "Unknown";

                    ReservationEvent event = ReservationEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .reservationId(reservation.getId())
                            .roomId(reservation.getRoomId())
                            .roomName(roomName)
                            .userEmail(reservation.getUserEmail())
                            .startTime(reservation.getStartTime())
                            .endTime(reservation.getEndTime())
                            .eventType("CANCELLED")
                            .timestamp(LocalDateTime.now())
                            .build();

                    reservationEventSink.tryEmitNext(event);
                    return true;
                })
                .subscribeOn(jpaScheduler)
                .then();
    }

    public Flux<ReservationResponseDTO> getAllReservations() {
        return Mono.fromCallable(() -> {
                    List<Reservation> reservations = reservationRepository.findAll();
                    return reservations.stream().map(res -> {
                        String roomName = roomRepository.findById(res.getRoomId())
                                .map(Room::getName).orElse("Unknown");
                        return mapToResponseDTO(res, roomName);
                    }).toList();
                })
                .subscribeOn(jpaScheduler)
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<ReservationEvent> getEventStream() {
        return reservationEventSink.asFlux();
    }

    private ReservationResponseDTO mapToResponseDTO(Reservation reservation, String roomName) {
        return ReservationResponseDTO.builder()
                .id(reservation.getId())
                .roomId(reservation.getRoomId())
                .roomName(roomName)
                .userEmail(reservation.getUserEmail())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .reminderSent(reservation.getReminderSent())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
