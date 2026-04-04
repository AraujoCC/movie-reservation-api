package com.isaac.moviereservation.dto.session;

import com.isaac.moviereservation.domain.entity.Session;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
 
public record SessionResponse(
    UUID id,
    UUID movieId,
    String movieTitle,
    UUID roomId,
    String roomName,
    String theaterName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    BigDecimal price
) {
    public static SessionResponse from(Session s) {
        return new SessionResponse(
            s.getId(),
            s.getMovie().getId(),
            s.getMovie().getTitle(),
            s.getRoom().getId(),
            s.getRoom().getName(),
            s.getRoom().getTheater().getName(),
            s.getStartTime(),
            s.getEndTime(),
            s.getPrice()
        );
    }
}
