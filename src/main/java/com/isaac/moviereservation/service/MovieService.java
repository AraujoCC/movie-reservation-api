package com.isaac.moviereservation.service;
 
import com.isaac.moviereservation.domain.entity.Movie;
import com.isaac.moviereservation.dto.movie.MovieRequest;
import com.isaac.moviereservation.dto.movie.MovieResponse;
import com.isaac.moviereservation.exception.ResourceNotFoundException;
import com.isaac.moviereservation.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDate;
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
public class MovieService {
 
    private final MovieRepository movieRepository;
 
    public Page<MovieResponse> findAll(String genre, String actor, LocalDate fromDate, Pageable pageable) {
        return movieRepository.findWithFilters(genre, actor, fromDate, pageable)
                .map(MovieResponse::from);
    }
 
    public MovieResponse findById(UUID id) {
        return movieRepository.findById(id)
                .map(MovieResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));
    }
 
    @Transactional
    public MovieResponse create(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.title())
                .description(request.description())
                .genre(request.genre())
                .director(request.director())
                .castMembers(request.castMembers())
                .durationMinutes(request.durationMinutes())
                .releaseDate(request.releaseDate())
                .posterUrl(request.posterUrl())
                .basePrice(request.basePrice())
                .build();
        return MovieResponse.from(movieRepository.save(movie));
    }
 
    @Transactional
    public MovieResponse update(UUID id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));
 
        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setGenre(request.genre());
        movie.setDirector(request.director());
        movie.setCastMembers(request.castMembers());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setReleaseDate(request.releaseDate());
        movie.setPosterUrl(request.posterUrl());
        movie.setBasePrice(request.basePrice());
 
        return MovieResponse.from(movieRepository.save(movie));
    }
 
    @Transactional
    public void delete(UUID id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie", id);
        }
        movieRepository.deleteById(id);
    }
}