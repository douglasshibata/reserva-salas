package com.reservasalas.booking.web;

import com.reservasalas.booking.dto.ErrorResponseDTO;
import com.reservasalas.booking.exception.InvalidReservationException;
import com.reservasalas.booking.exception.ReservationConflictException;
import com.reservasalas.booking.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalReactiveExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleNotFound(ResourceNotFoundException ex, ServerWebExchange exchange) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorDTO(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), exchange.getRequest().getPath().value())));
    }

    @ExceptionHandler(ReservationConflictException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleConflict(ReservationConflictException ex, ServerWebExchange exchange) {
        log.warn("Conflito de reserva: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorDTO(HttpStatus.CONFLICT, "Reservation Conflict", ex.getMessage(), exchange.getRequest().getPath().value())));
    }

    @ExceptionHandler(InvalidReservationException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleInvalid(InvalidReservationException ex, ServerWebExchange exchange) {
        log.warn("Reserva inválida: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorDTO(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage(), exchange.getRequest().getPath().value())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("Erro interno do servidor", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), exchange.getRequest().getPath().value())));
    }

    private ErrorResponseDTO buildErrorDTO(HttpStatus status, String error, String message, String path) {
        return ErrorResponseDTO.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}
