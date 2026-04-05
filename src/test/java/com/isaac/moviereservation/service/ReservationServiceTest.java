package com.isaac.moviereservation.service;

import com.isaac.moviereservation.domain.entity.*;
import com.isaac.moviereservation.domain.enums.ReservationStatus;
import com.isaac.moviereservation.domain.enums.SeatType;
import com.isaac.moviereservation.domain.enums.UserRole;
import com.isaac.moviereservation.dto.reservation.ReservationRequest;
import com.isaac.moviereservation.dto.reservation.ReservationResponse;
import com.isaac.moviereservation.exception.BusinessException;
import com.isaac.moviereservation.exception.ConflictException;
import com.isaac.moviereservation.exception.ResourceNotFoundException;
import com.isaac.moviereservation.repository.ReservationRepository;
import com.isaac.moviereservation.repository.SeatRepository;
import com.isaac.moviereservation.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService")
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private SeatRepository seatRepository;

    @InjectMocks
    private ReservationService reservationService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private User user;
    private Session session;
    private Room room;
    private Movie movie;
    private Seat seat1, seat2;
    private UUID sessionId, seatId1, seatId2;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        seatId1   = UUID.randomUUID();
        seatId2   = UUID.randomUUID();

        user = User.builder()
                .id(UUID.randomUUID())
                .name("Isaac")
                .email("isaac@email.com")
                .password("encoded-pass")
                .role(UserRole.ROLE_USER)
                .build();

        movie = Movie.builder()
                .id(UUID.randomUUID())
                .title("Inception")
                .build();

        room = Room.builder()
                .id(UUID.randomUUID())
                .name("Sala 1")
                .build();

        session = Session.builder()
                .id(sessionId)
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusHours(2))
                .price(new BigDecimal("25.00"))
                .build();

        seat1 = Seat.builder()
                .id(seatId1)
                .room(room)
                .rowLabel("A")
                .seatNumber(1)
                .type(SeatType.STANDARD)
                .build();

        seat2 = Seat.builder()
                .id(seatId2)
                .room(room)
                .rowLabel("A")
                .seatNumber(2)
                .type(SeatType.STANDARD)
                .build();
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar reserva com sucesso para assentos disponíveis")
        void shouldCreateReservationSuccessfully() {
            var request = new ReservationRequest(sessionId, List.of(seatId1, seatId2));

            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            when(seatRepository.findByIdInWithLock(List.of(seatId1, seatId2)))
                    .thenReturn(List.of(seat1, seat2));
            when(reservationRepository.existsConflictingReservation(sessionId, List.of(seatId1, seatId2)))
                    .thenReturn(false);
            when(reservationRepository.save(any())).thenAnswer(inv -> {
                Reservation r = inv.getArgument(0);
                // simula o @PrePersist
                var now = LocalDateTime.now();
                r.setCreatedAt(now);
                r.setExpiresAt(now.plusMinutes(15));
                r.setId(UUID.randomUUID());
                return r;
            });

            ReservationResponse response = reservationService.create(request, user);

            assertThat(response).isNotNull();
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
            assertThat(response.totalPrice()).isEqualByComparingTo("50.00"); // 2 assentos × R$25
            assertThat(response.seats()).hasSize(2);

            // verifica que o save foi chamado com os dados corretos
            var captor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(captor.capture());
            Reservation saved = captor.getValue();
            assertThat(saved.getUser()).isEqualTo(user);
            assertThat(saved.getSeats()).containsExactlyInAnyOrder(seat1, seat2);
            assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando sessão não existe")
        void shouldThrowWhenSessionNotFound() {
            var request = new ReservationRequest(sessionId, List.of(seatId1));
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.create(request, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(seatRepository, reservationRepository);
        }

        @Test
        @DisplayName("deve lançar BusinessException quando sessão já iniciou")
        void shouldThrowWhenSessionAlreadyStarted() {
            session.setStartTime(LocalDateTime.now().minusHours(1)); // sessão no passado
            var request = new ReservationRequest(sessionId, List.of(seatId1));
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> reservationService.create(request, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already started");

            verifyNoInteractions(seatRepository, reservationRepository);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando assento não existe")
        void shouldThrowWhenSeatNotFound() {
            var request = new ReservationRequest(sessionId, List.of(seatId1, seatId2));
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            // retorna apenas 1 dos 2 assentos solicitados
            when(seatRepository.findByIdInWithLock(anyList())).thenReturn(List.of(seat1));

            assertThatThrownBy(() -> reservationService.create(request, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(reservationRepository);
        }

        @Test
        @DisplayName("deve lançar BusinessException quando assento pertence a outra sala")
        void shouldThrowWhenSeatBelongsToWrongRoom() {
            Room outroRoom = Room.builder().id(UUID.randomUUID()).name("Sala 2").build();
            Seat assentoOutraSala = Seat.builder()
                    .id(seatId2).room(outroRoom).rowLabel("B").seatNumber(1).type(SeatType.STANDARD)
                    .build();

            var request = new ReservationRequest(sessionId, List.of(seatId1, seatId2));
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            when(seatRepository.findByIdInWithLock(anyList()))
                    .thenReturn(List.of(seat1, assentoOutraSala));

            assertThatThrownBy(() -> reservationService.create(request, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("do not belong");

            verifyNoInteractions(reservationRepository);
        }

        @Test
        @DisplayName("deve lançar ConflictException quando assento já está reservado")
        void shouldThrowWhenSeatAlreadyReserved() {
            var request = new ReservationRequest(sessionId, List.of(seatId1, seatId2));
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            when(seatRepository.findByIdInWithLock(anyList())).thenReturn(List.of(seat1, seat2));
            when(reservationRepository.existsConflictingReservation(any(), anyList()))
                    .thenReturn(true);

            assertThatThrownBy(() -> reservationService.create(request, user))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already reserved");

            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve calcular preço total corretamente para assento único")
        void shouldCalculateTotalPriceForSingleSeat() {
            var request = new ReservationRequest(sessionId, List.of(seatId1));
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            when(seatRepository.findByIdInWithLock(List.of(seatId1))).thenReturn(List.of(seat1));
            when(reservationRepository.existsConflictingReservation(any(), anyList())).thenReturn(false);
            when(reservationRepository.save(any())).thenAnswer(inv -> {
                Reservation r = inv.getArgument(0);
                r.setId(UUID.randomUUID());
                r.setCreatedAt(LocalDateTime.now());
                r.setExpiresAt(LocalDateTime.now().plusMinutes(15));
                return r;
            });

            ReservationResponse response = reservationService.create(request, user);

            assertThat(response.totalPrice()).isEqualByComparingTo("25.00");
        }
    }

    // ── cancel() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        private Reservation pendingReservation;

        @BeforeEach
        void setUp() {
            pendingReservation = Reservation.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .session(session)
                    .seats(List.of(seat1))
                    .status(ReservationStatus.PENDING)
                    .totalPrice(new BigDecimal("25.00"))
                    .build();
        }

        @Test
        @DisplayName("deve cancelar reserva PENDING com sucesso")
        void shouldCancelPendingReservation() {
            when(reservationRepository.findById(pendingReservation.getId()))
                    .thenReturn(Optional.of(pendingReservation));
            when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ReservationResponse response = reservationService.cancel(pendingReservation.getId(), user);

            assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
            verify(reservationRepository).save(argThat(r -> r.getStatus() == ReservationStatus.CANCELLED));
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando reserva não existe")
        void shouldThrowWhenReservationNotFound() {
            UUID randomId = UUID.randomUUID();
            when(reservationRepository.findById(randomId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.cancel(randomId, user))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não é dono da reserva")
        void shouldThrowWhenUserDoesNotOwnReservation() {
            User outroUsuario = User.builder().id(UUID.randomUUID()).build();
            when(reservationRepository.findById(pendingReservation.getId()))
                    .thenReturn(Optional.of(pendingReservation));

            assertThatThrownBy(() -> reservationService.cancel(pendingReservation.getId(), outroUsuario))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar BusinessException quando reserva já está cancelada")
        void shouldThrowWhenAlreadyCancelled() {
            pendingReservation.setStatus(ReservationStatus.CANCELLED);
            when(reservationRepository.findById(pendingReservation.getId()))
                    .thenReturn(Optional.of(pendingReservation));

            assertThatThrownBy(() -> reservationService.cancel(pendingReservation.getId(), user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando reserva está CONFIRMED")
        void shouldThrowWhenConfirmed() {
            pendingReservation.setStatus(ReservationStatus.CONFIRMED);
            when(reservationRepository.findById(pendingReservation.getId()))
                    .thenReturn(Optional.of(pendingReservation));

            assertThatThrownBy(() -> reservationService.cancel(pendingReservation.getId(), user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Confirmed reservations");
        }
    }

    // ── findById() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById()")
    class FindById {

        private Reservation reservation;

        @BeforeEach
        void setUp() {
            reservation = Reservation.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .session(session)
                    .seats(List.of(seat1))
                    .status(ReservationStatus.PENDING)
                    .totalPrice(new BigDecimal("25.00"))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();
        }

        @Test
        @DisplayName("usuário pode ver a própria reserva")
        void shouldReturnReservationForOwner() {
            when(reservationRepository.findById(reservation.getId()))
                    .thenReturn(Optional.of(reservation));

            ReservationResponse response = reservationService.findById(reservation.getId(), user);

            assertThat(response.id()).isEqualTo(reservation.getId());
        }

        @Test
        @DisplayName("admin pode ver reserva de qualquer usuário")
        void shouldReturnReservationForAdmin() {
            User admin = User.builder()
                    .id(UUID.randomUUID())
                    .role(UserRole.ROLE_ADMIN)
                    .build();
            when(reservationRepository.findById(reservation.getId()))
                    .thenReturn(Optional.of(reservation));

            ReservationResponse response = reservationService.findById(reservation.getId(), admin);

            assertThat(response.id()).isEqualTo(reservation.getId());
        }

        @Test
        @DisplayName("usuário não pode ver reserva de outro usuário")
        void shouldThrowWhenUserTriesToSeeAnotherUsersReservation() {
            User outroUsuario = User.builder()
                    .id(UUID.randomUUID())
                    .role(UserRole.ROLE_USER)
                    .build();
            when(reservationRepository.findById(reservation.getId()))
                    .thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.findById(reservation.getId(), outroUsuario))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── cancelExpiredReservations() ───────────────────────────────────────────

    @Nested
    @DisplayName("cancelExpiredReservations() — scheduler")
    class CancelExpired {

        @Test
        @DisplayName("deve cancelar todas as reservas PENDING expiradas")
        void shouldCancelExpiredPendingReservations() {
            Reservation r1 = Reservation.builder()
                    .id(UUID.randomUUID()).status(ReservationStatus.PENDING).build();
            Reservation r2 = Reservation.builder()
                    .id(UUID.randomUUID()).status(ReservationStatus.PENDING).build();

            when(reservationRepository.findExpiredPendingReservations(any()))
                    .thenReturn(List.of(r1, r2));

            reservationService.cancelExpiredReservations();

            assertThat(r1.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            assertThat(r2.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            verify(reservationRepository).saveAll(List.of(r1, r2));
        }

        @Test
        @DisplayName("não deve chamar saveAll quando não há reservas expiradas")
        void shouldDoNothingWhenNoExpiredReservations() {
            when(reservationRepository.findExpiredPendingReservations(any()))
                    .thenReturn(List.of());

            reservationService.cancelExpiredReservations();

            verify(reservationRepository, never()).saveAll(any());
        }
    }
}