package com.isaac.moviereservation.repository;

import com.isaac.moviereservation.domain.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Query("""
        SELECT s FROM Session s
        WHERE s.movie.id  = :movieId
          AND s.startTime >= :from
          AND s.startTime <  :to
        ORDER BY s.startTime
        """)
    List<Session> findByMovieAndDate(
            @Param("movieId") UUID movieId,
            @Param("from")    LocalDateTime from,
            @Param("to")      LocalDateTime to
    );
}