package com.cartumio.gate.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.request.token.TokenInvalidationRequest;
import com.cartumio.gate.dto.request.token.TokenVerificationRequest;
import com.cartumio.gate.dto.response.token.TokenVerificationResponse;
import com.cartumio.gate.service.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("TokenController - Tests")
class TokenControllerTest {

  private TokenController tokenController;
  private TokenService tokenService;
  private TokenVerificationRequest verificationRequest;
  private TokenInvalidationRequest invalidationRequest;
  private String tokenValue;
  private TokenType tokenType;

  @BeforeEach
  void setUp() {
    tokenService = mock(TokenService.class);
    tokenController = new TokenController(tokenService);

    tokenValue = "test-token-123";
    tokenType = TokenType.EMAIL_CONFIRMATION;
    verificationRequest = new TokenVerificationRequest(tokenValue, tokenType);
    invalidationRequest = new TokenInvalidationRequest(tokenValue);
  }

  @Test
  @DisplayName("Should verify token successfully and return valid response")
  void testVerifyTokenSuccessfully() {
    TokenVerificationResponse validationResult = new TokenVerificationResponse(true, false, false);

    when(tokenService.validateTokenWithDetails(tokenValue, tokenType)).thenReturn(validationResult);

    ResponseEntity<TokenVerificationResponse> response =
        tokenController.verifyToken(verificationRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().valid());
    assertFalse(response.getBody().expired());
    assertFalse(response.getBody().consumed());
    verify(tokenService).validateTokenWithDetails(tokenValue, tokenType);
  }

  @Test
  @DisplayName("Should return invalid response when token is expired")
  void testVerifyTokenExpired() {
    TokenVerificationResponse validationResult = new TokenVerificationResponse(false, true, false);

    when(tokenService.validateTokenWithDetails(tokenValue, tokenType)).thenReturn(validationResult);

    ResponseEntity<TokenVerificationResponse> response =
        tokenController.verifyToken(verificationRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().valid());
    assertTrue(response.getBody().expired());
    assertFalse(response.getBody().consumed());
    verify(tokenService).validateTokenWithDetails(tokenValue, tokenType);
  }

  @Test
  @DisplayName("Should return invalid response when token is consumed")
  void testVerifyTokenConsumed() {
    TokenVerificationResponse validationResult = new TokenVerificationResponse(false, false, true);

    when(tokenService.validateTokenWithDetails(tokenValue, tokenType)).thenReturn(validationResult);

    ResponseEntity<TokenVerificationResponse> response =
        tokenController.verifyToken(verificationRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().valid());
    assertFalse(response.getBody().expired());
    assertTrue(response.getBody().consumed());
    verify(tokenService).validateTokenWithDetails(tokenValue, tokenType);
  }

  @Test
  @DisplayName("Should return invalid response when token not found")
  void testVerifyTokenNotFound() {
    TokenVerificationResponse validationResult = new TokenVerificationResponse(false, false, false);

    when(tokenService.validateTokenWithDetails(tokenValue, tokenType)).thenReturn(validationResult);

    ResponseEntity<TokenVerificationResponse> response =
        tokenController.verifyToken(verificationRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().valid());
    assertFalse(response.getBody().expired());
    assertFalse(response.getBody().consumed());
    verify(tokenService).validateTokenWithDetails(tokenValue, tokenType);
  }

  @Test
  @DisplayName("Should invalidate token successfully and return 200 OK")
  void testInvalidateTokenSuccessfully() {
    when(tokenService.invalidateToken(tokenValue)).thenReturn(true);

    ResponseEntity<Void> response = tokenController.invalidateToken(invalidationRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(tokenService).invalidateToken(tokenValue);
  }

  @Test
  @DisplayName("Should return 404 NOT FOUND when token not found for invalidation")
  void testInvalidateTokenNotFound() {
    when(tokenService.invalidateToken(tokenValue)).thenReturn(false);

    ResponseEntity<Void> response = tokenController.invalidateToken(invalidationRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(tokenService).invalidateToken(tokenValue);
  }

  @Test
  @DisplayName("Should return 404 NOT FOUND when token already consumed")
  void testInvalidateTokenAlreadyConsumed() {
    when(tokenService.invalidateToken(tokenValue)).thenReturn(false);

    ResponseEntity<Void> response = tokenController.invalidateToken(invalidationRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(tokenService).invalidateToken(tokenValue);
  }
}
