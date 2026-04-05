package com.isaac.moviereservation.controller;

import com.isaac.moviereservation.domain.entity.User;
import com.isaac.moviereservation.dto.reservation.ReservationRequest;
import com.isaac.moviereservation.dto.reservation.ReservationResponse;
import com.isaac.moviereservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // POST /api/reservations
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid ReservationRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.create(request, user));
    }

    // GET /api/reservations  — reservas do usuário autenticado
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findMine(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(reservationService.findByUser(user));
    }

    // GET /api/reservations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(reservationService.findById(id, user));
    }

    // DELETE /api/reservations/{id}  — cancela reserva PENDING
    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(reservationService.cancel(id, user));
    }
}