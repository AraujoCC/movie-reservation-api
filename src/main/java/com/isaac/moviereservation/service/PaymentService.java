package com.isaac.moviereservation.service;
 
import com.isaac.moviereservation.domain.entity.Payment;
import com.isaac.moviereservation.domain.entity.Reservation;
import com.isaac.moviereservation.domain.entity.User;
import com.isaac.moviereservation.domain.enums.PaymentStatus;
import com.isaac.moviereservation.domain.enums.ReservationStatus;
import com.isaac.moviereservation.dto.payment.PaymentIntentResponse;
import com.isaac.moviereservation.dto.payment.PaymentResponse;
import com.isaac.moviereservation.exception.BusinessException;
import com.isaac.moviereservation.exception.ResourceNotFoundException;
import com.isaac.moviereservation.repository.PaymentRepository;
import com.isaac.moviereservation.repository.ReservationRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
 
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
 
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationEventProducer eventProducer;   // ← novo
 
    @Value("${stripe.api-key}")
    private String stripeApiKey;
 
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;
 
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
 
    // ── Criar PaymentIntent ───────────────────────────────────────────────────
    @Transactional
    public PaymentIntentResponse createIntent(UUID reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
 
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Reservation", reservationId);
        }
 
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("Only PENDING reservations can be paid");
        }
 
        paymentRepository.findByReservationId(reservationId).ifPresent(p -> {
            throw new BusinessException("A payment already exists for this reservation");
        });
 
        try {
            long amountInCents = reservation.getTotalPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();
 
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("brl")
                    .putMetadata("reservationId", reservationId.toString())
                    .putMetadata("userId", user.getId().toString())
                    .build();
 
            PaymentIntent intent = PaymentIntent.create(params);
 
            Payment payment = Payment.builder()
                    .reservation(reservation)
                    .stripePaymentIntentId(intent.getId())
                    .status(PaymentStatus.PENDING)
                    .amount(reservation.getTotalPrice())
                    .build();
 
            paymentRepository.save(payment);
 
            log.info("PaymentIntent {} created for reservation {}", intent.getId(), reservationId);
 
            return new PaymentIntentResponse(
                    intent.getClientSecret(),
                    intent.getId(),
                    reservation.getTotalPrice(),
                    reservationId
            );
 
        } catch (StripeException e) {
            log.error("Stripe error creating PaymentIntent: {}", e.getMessage());
            throw new BusinessException("Payment processing error: " + e.getMessage());
        }
    }
 
    // ── Webhook do Stripe ─────────────────────────────────────────────────────
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
 
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            throw new BusinessException("Invalid webhook signature");
        }
 
        log.info("Stripe webhook received: {}", event.getType());
 
        switch (event.getType()) {
            case "payment_intent.succeeded"      -> handlePaymentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            default -> log.debug("Unhandled Stripe event: {}", event.getType());
        }
    }
 
    // ── Handlers internos ─────────────────────────────────────────────────────
 
    private void handlePaymentSucceeded(Event event) {
        String intentId = extractIntentId(event);
 
        Payment payment = paymentRepository.findByStripePaymentIntentId(intentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for intent: " + intentId));
 
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
 
        Reservation reservation = payment.getReservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
 
        log.info("Payment {} confirmed — reservation {} is now CONFIRMED",
                intentId, reservation.getId());
 
        // Publica o evento no Kafka para o consumer salvar no MongoDB
        eventProducer.publishConfirmed(reservation);
    }
 
    private void handlePaymentFailed(Event event) {
        String intentId = extractIntentId(event);
 
        paymentRepository.findByStripePaymentIntentId(intentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
 
            Reservation reservation = payment.getReservation();
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
 
            log.warn("Payment {} failed — reservation {} cancelled",
                    intentId, reservation.getId());
        });
    }
 
    private String extractIntentId(Event event) {
        return event.getDataObjectDeserializer()
                .getObject()
                .map(obj -> ((PaymentIntent) obj).getId())
                .orElseThrow(() -> new BusinessException("Could not deserialize Stripe event"));
    }
 
    // ── Consultar pagamento ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PaymentResponse findByReservation(UUID reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
 
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Reservation", reservationId);
        }
 
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment for reservation: " + reservationId));
 
        return PaymentResponse.from(payment);
    }
}