package com.reservasalas.booking.service;

import com.reservasalas.booking.dto.AvailabilityQueryDTO;
import com.reservasalas.booking.exception.ResourceNotFoundException;
import com.reservasalas.booking.model.Reservation;
import com.reservasalas.booking.model.Room;
import com.reservasalas.booking.repository.ReservationRepository;
import com.reservasalas.booking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CachedAvailabilityService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    @Cacheable(value = "availability", key = "(#roomId != null ? #roomId : 'null') + '-' + (#minCapacity != null ? #minCapacity : 'null') + '-' + #startTime + '-' + #endTime")
    public List<AvailabilityQueryDTO> getAvailabilityCached(Long roomId, Integer minCapacity, LocalDateTime startTime, LocalDateTime endTime) {
        List<Room> rooms;
        if (roomId != null) {
            Room r = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com o ID: " + roomId));
            rooms = List.of(r);
        } else if (minCapacity != null) {
            rooms = roomRepository.findByCapacityGreaterThanEqualAndActiveTrue(minCapacity);
        } else {
            rooms = roomRepository.findByActiveTrue();
        }

        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now();
        LocalDateTime end = endTime != null ? endTime : start.plusHours(1);

        return rooms.stream().map(room -> {
            List<Reservation> conflicts = reservationRepository.findOverlappingReservations(room.getId(), start, end);
            boolean available = conflicts.isEmpty();
            return AvailabilityQueryDTO.builder()
                    .roomId(room.getId())
                    .roomName(room.getName())
                    .capacity(room.getCapacity())
                    .location(room.getLocation())
                    .checkedStartTime(start)
                    .checkedEndTime(end)
                    .isAvailable(available)
                    .conflictingReservationsCount(conflicts.size())
                    .build();
        }).toList();
    }
}
