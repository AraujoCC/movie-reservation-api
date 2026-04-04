package com.isaac.moviereservation.repository;

import com.isaac.moviereservation.domain.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TheaterRepository extends JpaRepository<Theater, UUID> {
}