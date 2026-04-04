package com.isaac.moviereservation.dto.movie;
 
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
 
public record MovieRequest(
 
    @NotBlank @Size(max = 200)
    String title,
 
    String description,
 
    @Size(max = 100)
    String genre,
 
    @Size(max = 200)
    String director,
 
    String castMembers,
 
    @Positive
    Integer durationMinutes,
 
    LocalDate releaseDate,
 
    @Size(max = 500)
    String posterUrl,
 
    @NotNull @DecimalMin("0.01")
    BigDecimal basePrice
) {}