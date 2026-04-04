package com.isaac.moviereservation.repository;

import com.isaac.moviereservation.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByTheaterId(UUID theaterId);
}