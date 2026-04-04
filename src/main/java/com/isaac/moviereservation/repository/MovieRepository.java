package com.isaac.moviereservation.repository;

import com.isaac.moviereservation.domain.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {

    @Query("""
        SELECT m FROM Movie m
        WHERE (:genre    IS NULL OR LOWER(m.genre)       = LOWER(:genre))
          AND (:actor    IS NULL OR LOWER(m.castMembers) LIKE LOWER(CONCAT('%', :actor, '%')))
          AND (:fromDate IS NULL OR m.releaseDate        >= :fromDate)
        """)
    Page<Movie> findWithFilters(
            @Param("genre")    String genre,
            @Param("actor")    String actor,
            @Param("fromDate") LocalDate fromDate,
            Pageable pageable
    );
}