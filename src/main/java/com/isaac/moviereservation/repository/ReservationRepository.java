package com.isaac.moviereservation.repository;

import com.isaac.moviereservation.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserId(UUID userId);

    List<Reservation> findBySessionId(UUID sessionId);

    // Usada pelo @Scheduled para cancelar reservas PENDING expiradas
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.status = 'PENDING'
          AND r.expiresAt < :now
        """)
    List<Reservation> findExpiredPendingReservations(@Param("now") LocalDateTime now);

    // Verifica se algum dos assentos já está em uso na sessão (PENDING ou CONFIRMED)
    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        JOIN r.seats s
        WHERE r.session.id = :sessionId
          AND s.id IN :seatIds
          AND r.status IN ('PENDING', 'CONFIRMED')
        """)
    boolean existsConflictingReservation(
            @Param("sessionId") UUID sessionId,
            @Param("seatIds")   List<UUID> seatIds
    );
}