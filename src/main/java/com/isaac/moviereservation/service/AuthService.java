package com.isaac.moviereservation.service;

import com.isaac.moviereservation.config.JwtService;
import com.isaac.moviereservation.domain.entity.User;
import com.isaac.moviereservation.domain.enums.UserRole;
import com.isaac.moviereservation.dto.auth.AuthResponse;
import com.isaac.moviereservation.dto.auth.LoginRequest;
import com.isaac.moviereservation.dto.auth.RegisterRequest;
import com.isaac.moviereservation.exception.ConflictException;
import com.isaac.moviereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Service
@RequiredArgsConstructor
public class AuthService {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
 
    @Transactional
    public AuthResponse register(RegisterRequest request) throws ConflictException {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }
 
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_USER)
                .build();
 
        userRepository.save(user);
 
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
 
    public AuthResponse login(LoginRequest request) {
        // Delega ao AuthenticationManager — lança BadCredentialsException se falhar
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
 
        User user = userRepository.findByEmail(request.email())
                .orElseThrow();
 
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}