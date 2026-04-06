package com.isaac.moviereservation.service;
 
import com.isaac.moviereservation.domain.document.ReservationEventDocument;
import com.isaac.moviereservation.domain.event.ReservationConfirmedEvent;
import com.isaac.moviereservation.repository.ReservationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
 
import java.time.LocalDateTime;
 
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ReservationEventConsumer {
 
    private final ReservationEventRepository eventRepository;
 
    // Consome do tópico "reservations" usando o containerFactory configurado
    // no KafkaConfig com desserialização JSON para ReservationConfirmedEvent.
    @KafkaListener(
        topics = "reservations",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ReservationConfirmedEvent event) {
        log.info("Consuming ReservationConfirmedEvent for reservation {}",
                event.reservationId());
 
        try {
            ReservationEventDocument document = ReservationEventDocument.builder()
                    .reservationId(event.reservationId())
                    .userId(event.userId())
                    .userEmail(event.userEmail())
                    .sessionId(event.sessionId())
                    .movieTitle(event.movieTitle())
                    .sessionStartTime(event.sessionStartTime())
                    .seats(event.seats())
                    .totalPrice(event.totalPrice())
                    .confirmedAt(event.confirmedAt())
                    .savedAt(LocalDateTime.now())
                    .build();
 
            eventRepository.save(document);
 
            log.info("ReservationEventDocument saved to MongoDB — reservation {} user {}",
                    event.reservationId(), event.userEmail());
 
        } catch (Exception e) {
            // Log do erro sem relançar — evita reprocessamento infinito.
            // Em produção, configurar um Dead Letter Topic (DLT) para reprocessamento manual.
            log.error("Error saving ReservationEventDocument for reservation {}: {}",
                    event.reservationId(), e.getMessage(), e);
        }
    }
}