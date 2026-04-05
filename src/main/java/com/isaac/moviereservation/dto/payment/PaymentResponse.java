package com.isaac.moviereservation.dto.payment;
 
import com.isaac.moviereservation.domain.entity.Payment;
import com.isaac.moviereservation.domain.enums.PaymentStatus;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
 
public record PaymentResponse(
    UUID id,
    UUID reservationId,
    PaymentStatus status,
    BigDecimal amount,
    String stripePaymentIntentId,
    LocalDateTime paidAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getReservation().getId(),
                p.getStatus(),
                p.getAmount(),
                p.getStripePaymentIntentId(),
                p.getPaidAt()
        );
    }
}