package com.reservasalas.booking.service;

import com.reservasalas.booking.dto.RoomDTO;
import com.reservasalas.booking.exception.ResourceNotFoundException;
import com.reservasalas.booking.model.Room;
import com.reservasalas.booking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveRoomService {

    private final RoomRepository roomRepository;
    private final CachedRoomService cachedRoomService;
    private final Scheduler jpaScheduler;

    public Flux<RoomDTO> getAllActiveRooms() {
        return Mono.fromCallable(cachedRoomService::getAllActiveRoomsCached)
                .subscribeOn(jpaScheduler)
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<RoomDTO> getRoomById(Long id) {
        return Mono.fromCallable(() -> roomRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada com o ID: " + id)))
                .subscribeOn(jpaScheduler)
                .map(this::mapToDTO);
    }

    public Mono<RoomDTO> createRoom(RoomDTO dto) {
        return Mono.fromCallable(() -> {
                    Room room = Room.builder()
                            .name(dto.getName())
                            .capacity(dto.getCapacity())
                            .location(dto.getLocation())
                            .features(dto.getFeatures())
                            .active(dto.getActive() != null ? dto.getActive() : true)
                            .build();
                    return cachedRoomService.saveAndEvictRoomCache(room);
                })
                .subscribeOn(jpaScheduler);
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
