package com.reservasalas.booking.web;

import com.reservasalas.booking.dto.AvailabilityQueryDTO;
import com.reservasalas.booking.dto.RoomDTO;
import com.reservasalas.booking.service.ReactiveReservationService;
import com.reservasalas.booking.service.ReactiveRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/salas")
@RequiredArgsConstructor
public class ReactiveRoomController {

    private final ReactiveRoomService roomService;
    private final ReactiveReservationService reservationService;

    @GetMapping
    public Flux<RoomDTO> getAllActiveRooms() {
        return roomService.getAllActiveRooms();
    }

    @GetMapping("/{id}")
    public Mono<RoomDTO> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RoomDTO> createRoom(@RequestBody RoomDTO dto) {
        return roomService.createRoom(dto);
    }

    @GetMapping("/disponibilidade")
    public Flux<AvailabilityQueryDTO> checkAvailability(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return reservationService.checkAvailability(roomId, minCapacity, startTime, endTime);
    }
}
