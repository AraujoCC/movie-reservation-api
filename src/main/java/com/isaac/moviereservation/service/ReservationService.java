package com.isaac.moviereservation.service;

import com.isaac.moviereservation.domain.entity.*;
import com.isaac.moviereservation.domain.enums.ReservationStatus;
import com.isaac.moviereservation.dto.reservation.ReservationRequest;
import com.isaac.moviereservation.dto.reservation.ReservationResponse;
import com.isaac.moviereservation.exception.BusinessException;
import com.isaac.moviereservation.exception.ConflictException;
import com.isaac.moviereservation.exception.ResourceNotFoundException;
import com.isaac.moviereservation.repository.ReservationRepository;
import com.isaac.moviereservation.repository.SeatRepository;
import com.isaac.moviereservation.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;

    // ── Criar reserva ─────────────────────────────────────────────────────────
    //
    // @Transactional garante que o lock é mantido até o COMMIT.
    // Se qualquer coisa falhar, o ROLLBACK libera os locks automaticamente.
    //
    @Transactional
    public ReservationResponse create(ReservationRequest request, User user) {

        // 1. Carrega a sessão
        Session session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session", request.sessionId()));

        // 2. Valida que a sessão ainda não começou
        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot reserve seats for a session that has already started");
        }

        // 3. SELECT FOR UPDATE — bloqueia as linhas dos assentos no PostgreSQL.
        //    Qualquer outra transação que tentar os mesmos assentos ficará
        //    esperando até este método terminar (commit ou rollback).
        List<Seat> seats = seatRepository.findByIdInWithLock(request.seatIds());

        // 4. Valida que todos os seatIds existem e pertencem à sala da sessão
        if (seats.size() != request.seatIds().size()) {
            throw new ResourceNotFoundException("One or more seats not found");
        }

        UUID roomId = session.getRoom().getId();
        boolean wrongRoom = seats.stream()
                .anyMatch(s -> !s.getRoom().getId().equals(roomId));
        if (wrongRoom) {
            throw new BusinessException("One or more seats do not belong to this session's room");
        }

        // 5. Com o lock ativo, verifica conflito no banco
        //    (segunda camada de proteção além do UNIQUE constraint)
        boolean conflict = reservationRepository.existsConflictingReservation(
                request.sessionId(), request.seatIds()
        );
        if (conflict) {
            throw new ConflictException("One or more seats are already reserved for this session");
        }

        // 6. Calcula o preço total
        BigDecimal totalPrice = session.getPrice()
                .multiply(BigDecimal.valueOf(seats.size()));

        // 7. Cria e persiste a reserva
        Reservation reservation = Reservation.builder()
                .user(user)
                .session(session)
                .seats(seats)
                .status(ReservationStatus.PENDING)
                .totalPrice(totalPrice)
                .build();

        reservationRepository.save(reservation);

        log.info("Reservation {} created for user {} — {} seats, total {}",
                reservation.getId(), user.getEmail(), seats.size(), totalPrice);

        return ReservationResponse.from(reservation);
    }

    // ── Listar reservas do usuário autenticado ────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReservationResponse> findByUser(User user) {
        return reservationRepository.findByUserId(user.getId())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // ── Buscar reserva por ID ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ReservationResponse findById(UUID id, User user) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));

        // Usuário só pode ver a própria reserva (admins podem ver qualquer uma)
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !reservation.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Reservation", id);
        }

        return ReservationResponse.from(reservation);
    }

    // ── Cancelar reserva ──────────────────────────────────────────────────────
    @Transactional
    public ReservationResponse cancel(UUID id, User user) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Reservation", id);
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("Reservation is already cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            throw new BusinessException("Confirmed reservations cannot be cancelled directly — contact support");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        log.info("Reservation {} cancelled by user {}", id, user.getEmail());

        return ReservationResponse.from(reservation);
    }

    // ── Cron: cancela reservas PENDING expiradas ──────────────────────────────
    //
    // Roda a cada 60 segundos.
    // Se o usuário não pagou em 15 minutos, a reserva é liberada
    // e os assentos voltam a ficar disponíveis automaticamente.
    //
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cancelExpiredReservations() {
        List<Reservation> expired = reservationRepository
                .findExpiredPendingReservations(LocalDateTime.now());

        if (expired.isEmpty()) return;

        expired.forEach(r -> r.setStatus(ReservationStatus.CANCELLED));
        reservationRepository.saveAll(expired);

        log.info("Cancelled {} expired PENDING reservations", expired.size());
    }
}