package com.reservasalas.booking.web;

import com.reservasalas.booking.dto.ReservationEvent;
import com.reservasalas.booking.dto.ReservationRequestDTO;
import com.reservasalas.booking.dto.ReservationResponseDTO;
import com.reservasalas.booking.service.ReactiveReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReactiveBookingController {

    private final ReactiveReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReservationResponseDTO> createReservation(@RequestBody ReservationRequestDTO dto) {
        return reservationService.createReservation(dto);
    }

    @GetMapping
    public Flux<ReservationResponseDTO> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }

    @GetMapping(value = "/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ReservationEvent>> streamReservationEvents() {
        return reservationService.getEventStream()
                .map(event -> ServerSentEvent.<ReservationEvent>builder()
                        .id(event.getEventId())
                        .event(event.getEventType())
                        .data(event)
                        .build());
    }
}
