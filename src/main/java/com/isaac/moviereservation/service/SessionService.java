package com.isaac.moviereservation.service;

import com.isaac.moviereservation.dto.session.SeatAvailabilityResponse;
import com.isaac.moviereservation.dto.session.SessionResponse;
import com.isaac.moviereservation.exception.ResourceNotFoundException;
import com.isaac.moviereservation.repository.SeatRepository;
import com.isaac.moviereservation.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;

    public List<SessionResponse> findByMovieAndDate(UUID movieId, LocalDate date) {
        LocalDateTime from = (date != null) ? date.atStartOfDay() : LocalDateTime.now();
        LocalDateTime to = (date != null) ? date.plusDays(1).atStartOfDay() : from.plusYears(1);
        return sessionRepository.findByMovieAndDate(movieId, from, to)
                .stream().map(SessionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatAvailabilityResponse> getSeatAvailability(UUID sessionId) throws ResourceNotFoundException {
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", String.valueOf(sessionId)));

        List<UUID> allSeatIds = session.getRoom().getSeats()
                .stream().map(s -> s.getId()).toList();

        Set<UUID> occupiedIds = seatRepository.findOccupiedSeatsBySession(sessionId)
                .stream().map(s -> s.getId()).collect(Collectors.toSet());

        return session.getRoom().getSeats().stream()
                .map(seat -> SeatAvailabilityResponse.from(seat, !occupiedIds.contains(seat.getId())))
                .toList();
    }
}