package com.isaac.moviereservation.dto.reservation;
 
import com.isaac.moviereservation.domain.entity.Reservation;
import com.isaac.moviereservation.domain.entity.Seat;
import com.isaac.moviereservation.domain.enums.ReservationStatus;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
 
public record ReservationResponse(
    UUID id,
    UUID sessionId,
    String movieTitle,
    LocalDateTime sessionStartTime,
    ReservationStatus status,
    BigDecimal totalPrice,
    LocalDateTime createdAt,
    LocalDateTime expiresAt,
    List<SeatInfo> seats
) {
    public record SeatInfo(UUID id, String rowLabel, Integer seatNumber, String type) {}
 
    public static ReservationResponse from(Reservation r) {
        List<SeatInfo> seatInfos = r.getSeats().stream()
                .map(s -> new SeatInfo(s.getId(), s.getRowLabel(), s.getSeatNumber(), s.getType().name()))
                .toList();
 
        return new ReservationResponse(
                r.getId(),
                r.getSession().getId(),
                r.getSession().getMovie().getTitle(),
                r.getSession().getStartTime(),
                r.getStatus(),
                r.getTotalPrice(),
                r.getCreatedAt(),
                r.getExpiresAt(),
                seatInfos
        );
    }
}