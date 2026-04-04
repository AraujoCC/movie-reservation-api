package com.isaac.moviereservation.dto.movie;

import com.isaac.moviereservation.domain.entity.Movie;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
 
public record MovieResponse(
    UUID id,
    String title,
    String description,
    String genre,
    String director,
    String castMembers,
    Integer durationMinutes,
    LocalDate releaseDate,
    String posterUrl,
    BigDecimal basePrice
) {
    public static MovieResponse from(Movie m) {
        return new MovieResponse(
            m.getId(), m.getTitle(), m.getDescription(), m.getGenre(),
            m.getDirector(), m.getCastMembers(), m.getDurationMinutes(),
            m.getReleaseDate(), m.getPosterUrl(), m.getBasePrice()
        );
    }
}