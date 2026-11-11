package com.reservasalas.booking.repository;

import com.reservasalas.booking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByActiveTrue();

    List<Room> findByCapacityGreaterThanEqualAndActiveTrue(Integer capacity);
}
