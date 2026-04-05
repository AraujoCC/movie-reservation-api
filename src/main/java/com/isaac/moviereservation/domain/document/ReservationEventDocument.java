package com.isaac.moviereservation.domain.document;
 
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
 
// Documento MongoDB — salvo na collection "reservation_events".
// Serve como log de auditoria imutável e base para notificações futuras.
@Document(collection = "reservation_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEventDocument {
 
    @Id
    private String id;                      // ObjectId gerado pelo MongoDB
 
    private UUID reservationId;
    private UUID userId;
    private String userEmail;
    private UUID sessionId;
    private String movieTitle;
    private LocalDateTime sessionStartTime;
    private List<String> seats;
    private BigDecimal totalPrice;
    private LocalDateTime confirmedAt;
    private LocalDateTime savedAt;          // timestamp de quando o consumer salvou
}