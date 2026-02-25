package com.cartumio.gate.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
      EntityNotFoundException ex, HttpServletRequest request) {
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(EmailFailedException.class)
  public ResponseEntity<ErrorResponse> handleEmailFailedException(
      EmailFailedException ex, HttpServletRequest request) {
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .path(request.getRequestURI())
            .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .path(request.getRequestURI())
            .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
