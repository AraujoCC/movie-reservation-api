package com.isaac.moviereservation.dto.auth;

public record AuthResponse(
    String token,
    String email,
    String role
) {}
