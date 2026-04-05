package com.isaac.moviereservation.domain.event;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
 
// Evento publicado no Kafka quando uma reserva é confirmada via webhook do Stripe.
// Serializado como JSON pelo KafkaTemplate.
public record ReservationConfirmedEvent(
    UUID reservationId,
    UUID userId,
    String userEmail,
    UUID sessionId,
    String movieTitle,
    LocalDateTime sessionStartTime,
    List<String> seats,         // ex: ["A1", "A2"]
    BigDecimal totalPrice,
    LocalDateTime confirmedAt
) {}