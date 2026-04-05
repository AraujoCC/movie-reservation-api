package com.isaac.moviereservation.dto.reservation;
 
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
 
import java.util.List;
import java.util.UUID;
 
public record ReservationRequest(
 
    @NotNull(message = "sessionId is required")
    UUID sessionId,
 
    @NotEmpty(message = "At least one seat must be selected")
    List<UUID> seatIds
) {}