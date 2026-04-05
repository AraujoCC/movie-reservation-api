// src/main/java/com/isaac/moviereservation/controller/PaymentController.java
package com.isaac.moviereservation.controller;

import com.isaac.moviereservation.domain.entity.User;
import com.isaac.moviereservation.dto.payment.PaymentIntentResponse;
import com.isaac.moviereservation.dto.payment.PaymentResponse;
import com.isaac.moviereservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/payments/create-intent?reservationId=...
    // Usuário autenticado cria o PaymentIntent para pagar a reserva
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @RequestParam UUID reservationId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(paymentService.createIntent(reservationId, user));
    }

    // GET /api/payments?reservationId=...
    // Consulta o status do pagamento de uma reserva
    @GetMapping
    public ResponseEntity<PaymentResponse> findByReservation(
            @RequestParam UUID reservationId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(paymentService.findByReservation(reservationId, user));
    }

    // POST /api/payments/webhook
    // Chamado pelo Stripe — sem JWT, validado por assinatura HMAC
    // Precisa receber o body como String RAW (não deserializado pelo Spring)
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}