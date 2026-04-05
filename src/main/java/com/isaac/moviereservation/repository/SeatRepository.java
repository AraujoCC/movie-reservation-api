package com.isaac.moviereservation.repository;

import com.isaac.moviereservation.domain.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByRoomId(UUID roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :ids")
    List<Seat> findByIdInWithLock(@Param("ids") List<UUID> ids);

    // Navega a partir de Reservation — caminho correto no modelo de domínio:
    // Reservation → session → (filtra sessionId) → seats (join)
    @Query("""
        SELECT DISTINCT s FROM Seat s
        JOIN Reservation r ON s MEMBER OF r.seats
        WHERE r.session.id = :sessionId
          AND r.status IN ('PENDING', 'CONFIRMED')
        """)
    List<Seat> findOccupiedSeatsBySession(@Param("sessionId") UUID sessionId);
}