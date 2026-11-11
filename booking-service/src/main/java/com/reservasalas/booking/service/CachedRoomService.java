package com.reservasalas.booking.service;

import com.reservasalas.booking.dto.RoomDTO;
import com.reservasalas.booking.model.Room;
import com.reservasalas.booking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CachedRoomService {

    private final RoomRepository roomRepository;

    @Cacheable(value = "rooms", key = "'all'")
    public List<RoomDTO> getAllActiveRoomsCached() {
        return roomRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @CacheEvict(value = "rooms", allEntries = true)
    public RoomDTO saveAndEvictRoomCache(Room room) {
        Room saved = roomRepository.save(room);
        return mapToDTO(saved);
    }

    private RoomDTO mapToDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .location(room.getLocation())
                .features(room.getFeatures())
                .active(room.getActive())
                .build();
    }
}
