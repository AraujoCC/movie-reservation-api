package com.isaac.moviereservation.controller;

import com.isaac.moviereservation.dto.movie.MovieRequest;
import com.isaac.moviereservation.dto.movie.MovieResponse;
import com.isaac.moviereservation.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
import java.time.LocalDate;
import java.util.UUID;
 
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
 
    private final MovieService movieService;
 
    // GET /api/movies?genre=Action&actor=DiCaprio&fromDate=2024-01-01&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<MovieResponse>> findAll(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @PageableDefault(size = 10, sort = "title") Pageable pageable
    ) {
        return ResponseEntity.ok(movieService.findAll(genre, actor, fromDate, pageable));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(movieService.findById(id));
    }
 
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MovieResponse> create(@RequestBody @Valid MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(request));
    }
 
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MovieResponse> update(@PathVariable UUID id,
                                                @RequestBody @Valid MovieRequest request) {
        return ResponseEntity.ok(movieService.update(id, request));
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws com.isaac.moviereservation.exception.ResourceNotFoundException {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
