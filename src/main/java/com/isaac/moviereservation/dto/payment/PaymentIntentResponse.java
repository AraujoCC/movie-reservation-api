package com.isaac.moviereservation.dto.payment;
 
import java.math.BigDecimal;
import java.util.UUID;
 
public record PaymentIntentResponse(
    String clientSecret,       // enviado ao frontend para confirmar o pagamento
    String paymentIntentId,
    BigDecimal amount,
    UUID reservationId
) {}