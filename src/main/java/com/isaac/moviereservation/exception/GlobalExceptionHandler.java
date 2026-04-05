package com.isaac.moviereservation.exception;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — recurso não encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource Not Found");
        pd.setType(URI.create("/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 409 — conflito (ex: assento já reservado, e-mail duplicado)
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict");
        pd.setType(URI.create("/errors/conflict"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 422 — regra de negócio violada
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Business Rule Violation");
        pd.setType(URI.create("/errors/business"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 400 — falha de validação do Bean Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a // campo com múltiplos erros: mantém o primeiro
                ));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
        pd.setTitle("Validation Error");
        pd.setType(URI.create("/errors/validation"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("fields", fields);
        return pd;
    }

    // 401 — credenciais inválidas no login
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        pd.setTitle("Unauthorized");
        pd.setType(URI.create("/errors/unauthorized"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 403 — sem permissão (@PreAuthorize falhou)
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
        pd.setTitle("Forbidden");
        pd.setType(URI.create("/errors/forbidden"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex); // ← esta linha
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("/errors/internal"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}