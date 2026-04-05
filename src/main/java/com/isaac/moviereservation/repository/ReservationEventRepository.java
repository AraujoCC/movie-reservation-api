package com.isaac.moviereservation.repository;
 
import com.isaac.moviereservation.domain.document.ReservationEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
 
import java.util.List;
import java.util.UUID;
 
public interface ReservationEventRepository extends MongoRepository<ReservationEventDocument, String> {
 
    List<ReservationEventDocument> findByUserId(UUID userId);
 
    List<ReservationEventDocument> findByReservationId(UUID reservationId);
}