package com.isaac.moviereservation.dto.session;

import com.isaac.moviereservation.domain.entity.Seat;
import com.isaac.moviereservation.domain.enums.SeatType;
 
import java.util.UUID;
 
public record SeatAvailabilityResponse(
    UUID id,
    String rowLabel,
    Integer seatNumber,
    SeatType type,
    boolean available
) {
    public static SeatAvailabilityResponse from(Seat seat, boolean available) {
        return new SeatAvailabilityResponse(
            seat.getId(), seat.getRowLabel(), seat.getSeatNumber(), seat.getType(), available
        );
    }
}