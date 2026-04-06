package com.isaac.moviereservation.service;

import com.isaac.moviereservation.domain.entity.Reservation;
import com.isaac.moviereservation.domain.event.ReservationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ReservationEventProducer {

    private static final String TOPIC = "reservations";

    private final KafkaTemplate<String, ReservationConfirmedEvent> kafkaTemplate;

    public void publishConfirmed(Reservation reservation) {
        ReservationConfirmedEvent event = toEvent(reservation);

        // A key é o reservationId — garante que eventos da mesma reserva
        // vão sempre para a mesma partição (ordenação por reserva).
        CompletableFuture<SendResult<String, ReservationConfirmedEvent>> future =
                kafkaTemplate.send(TOPIC, reservation.getId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish ReservationConfirmedEvent for reservation {}: {}",
                        reservation.getId(), ex.getMessage());
            } else {
                log.info("ReservationConfirmedEvent published — reservation {} → partition {} offset {}",
                        reservation.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private ReservationConfirmedEvent toEvent(Reservation reservation) {
        // Formata os assentos como "A1", "B3", etc.
        var seats = reservation.getSeats().stream()
                .map(s -> s.getRowLabel() + s.getSeatNumber())
                .toList();

        return new ReservationConfirmedEvent(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getEmail(),
                reservation.getSession().getId(),
                reservation.getSession().getMovie().getTitle(),
                reservation.getSession().getStartTime(),
                seats,
                reservation.getTotalPrice(),
                LocalDateTime.now()
        );
    }
}