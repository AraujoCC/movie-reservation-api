package com.isaac.moviereservation.controller;

import com.isaac.moviereservation.dto.session.SeatAvailabilityResponse;
import com.isaac.moviereservation.dto.session.SessionResponse;
import com.isaac.moviereservation.service.SessionService;
import com.isaac.moviereservation.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
 
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
 
    private final SessionService sessionService;
 
    // GET /api/sessions?movieId=&date=2025-06-01
    @GetMapping
    public ResponseEntity<List<SessionResponse>> findSessions(
            @RequestParam UUID movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(sessionService.findByMovieAndDate(movieId, date));
    }
 
    // GET /api/sessions/{id}/seats — mapa de disponibilidade
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatAvailabilityResponse>> getSeatAvailability(@PathVariable UUID id) throws ResourceNotFoundException {
        return ResponseEntity.ok(sessionService.getSeatAvailability(id));
    }
}