package com.cartumio.gate.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("GlobalExceptionHandler - Tests")
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler globalExceptionHandler;
  private HttpServletRequest request;

  private static final String REQUEST_PATH = "/gate/v1/waitlist-users/create";
  private static final String ERROR_MESSAGE = "Entity not found";

  @BeforeEach
  void setUp() {
    globalExceptionHandler = new GlobalExceptionHandler();
    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn(REQUEST_PATH);
  }

  @Test
  @DisplayName("Should handle EntityNotFoundException and return 404 NOT_FOUND")
  void testHandleEntityNotFoundException() {
    EntityNotFoundException exception = new EntityNotFoundException(ERROR_MESSAGE);

    ResponseEntity<ErrorResponse> response =
        globalExceptionHandler.handleEntityNotFoundException(exception, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getBody().getError());
    assertEquals(ERROR_MESSAGE, response.getBody().getMessage());
    assertEquals(REQUEST_PATH, response.getBody().getPath());
    assertNotNull(response.getBody().getTimestamp());
  }

  @Test
  @DisplayName("Should handle EmailFailedException and return 500 INTERNAL_SERVER_ERROR")
  void testHandleEmailFailedException() {
    EmailFailedException exception = new EmailFailedException("Email sending failed");

    ResponseEntity<ErrorResponse> response =
        globalExceptionHandler.handleEmailFailedException(exception, request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.getBody().getError());
    assertEquals(REQUEST_PATH, response.getBody().getPath());
    assertNotNull(response.getBody().getTimestamp());
  }

  @Test
  @DisplayName("Should handle generic Exception and return 500 INTERNAL_SERVER_ERROR")
  void testHandleGenericException() {
    Exception exception = new RuntimeException("Unexpected error");

    ResponseEntity<ErrorResponse> response =
        globalExceptionHandler.handleGeneric(exception, request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.getBody().getError());
    assertEquals(REQUEST_PATH, response.getBody().getPath());
    assertNotNull(response.getBody().getTimestamp());
  }

  @Test
  @DisplayName("Should set timestamp in error response")
  void testErrorResponseHasTimestamp() {
    EntityNotFoundException exception = new EntityNotFoundException(ERROR_MESSAGE);

    ResponseEntity<ErrorResponse> response =
        globalExceptionHandler.handleEntityNotFoundException(exception, request);

    assertNotNull(response.getBody().getTimestamp());
    assert response.getBody().getTimestamp().isBefore(OffsetDateTime.now().plusSeconds(1))
            || response.getBody().getTimestamp().isEqual(OffsetDateTime.now().plusSeconds(1))
        : "Timestamp should be recent";
  }
}
