package com.isaac.moviereservation.controller;

import com.isaac.moviereservation.dto.auth.AuthResponse;
import com.isaac.moviereservation.dto.auth.LoginRequest;
import com.isaac.moviereservation.dto.auth.RegisterRequest;
import com.isaac.moviereservation.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
 
    private final AuthService authService;
 
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
 
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
