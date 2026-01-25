package com.cartumio.gate.service.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.domain.token.Token;
import com.cartumio.gate.domain.token.TokenType;
import com.cartumio.gate.dto.response.token.TokenResponse;
import com.cartumio.gate.repository.TokenRepository;

@DisplayName("TokenService - Tests")
class TokenServiceTest {

    private TokenService tokenService;
    private TokenRepository tokenRepository;
    private Token token;
    private String tokenValue;
    private TokenType tokenType;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(TokenRepository.class);
        tokenService = new TokenService(tokenRepository);

        tokenValue = UUID.randomUUID().toString();
        tokenType = TokenType.EMAIL_CONFIRMATION;

        token = new Token();
        token.setId(UUID.randomUUID());
        token.setToken(tokenValue);
        token.setTokenType(tokenType);
        token.setExpiresAt(Instant.now().plus(Duration.ofHours(24)));
        token.setConsumed(false);
    }

    @Test
    @DisplayName("Should generate token successfully with default expiration")
    void testGenerateTokenWithDefaultExpiration() {
        when(tokenRepository.existsByToken(any(String.class))).thenReturn(false);
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> {
            Token t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TokenResponse response = tokenService.generateToken(tokenType);

        assertNotNull(response);
        assertNotNull(response.token());
        assertNotNull(response.expiresAt());
        assertEquals(tokenType, response.tokenType());
        verify(tokenRepository).existsByToken(any(String.class));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    @DisplayName("Should generate token successfully with custom expiration")
    void testGenerateTokenWithCustomExpiration() {
        Duration customExpiration = Duration.ofHours(48);
        when(tokenRepository.existsByToken(any(String.class))).thenReturn(false);
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> {
            Token t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        Token generatedToken = tokenService.generateToken(tokenType, customExpiration);

        assertNotNull(generatedToken);
        assertNotNull(generatedToken.getToken());
        assertEquals(tokenType, generatedToken.getTokenType());
        assertFalse(generatedToken.isConsumed());
        assertTrue(generatedToken.getExpiresAt().isAfter(Instant.now()));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    @DisplayName("Should regenerate token if duplicate exists")
    void testGenerateTokenRegeneratesOnDuplicate() {
        when(tokenRepository.existsByToken(any(String.class)))
                .thenReturn(true)
                .thenReturn(false);
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> {
            Token t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TokenResponse response = tokenService.generateToken(tokenType);

        assertNotNull(response);
        verify(tokenRepository, atLeastOnce()).existsByToken(any(String.class));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    @DisplayName("Should validate token successfully when token is valid")
    void testValidateTokenSuccessfully() {
        token.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        token.setConsumed(false);
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.of(token));

        boolean isValid = tokenService.validateToken(tokenValue, tokenType);

        assertTrue(isValid);
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should return false when token is not found")
    void testValidateTokenNotFound() {
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.empty());

        boolean isValid = tokenService.validateToken(tokenValue, tokenType);

        assertFalse(isValid);
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should return false when token is expired")
    void testValidateTokenExpired() {
        token.setExpiresAt(Instant.now().minus(Duration.ofHours(1)));
        token.setConsumed(false);
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.of(token));

        boolean isValid = tokenService.validateToken(tokenValue, tokenType);

        assertFalse(isValid);
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should return false when token is consumed")
    void testValidateTokenConsumed() {
        token.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        token.setConsumed(true);
        token.setConsumedAt(Instant.now());
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.of(token));

        boolean isValid = tokenService.validateToken(tokenValue, tokenType);

        assertFalse(isValid);
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should validate token with details successfully")
    void testValidateTokenWithDetailsSuccessfully() {
        token.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        token.setConsumed(false);
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.of(token));

        TokenService.TokenValidationResult result = tokenService.validateTokenWithDetails(tokenValue, tokenType);

        assertTrue(result.isValid());
        assertFalse(result.isExpired());
        assertFalse(result.isConsumed());
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should return invalid result when token not found")
    void testValidateTokenWithDetailsNotFound() {
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.empty());

        TokenService.TokenValidationResult result = tokenService.validateTokenWithDetails(tokenValue, tokenType);

        assertFalse(result.isValid());
        assertFalse(result.isExpired());
        assertFalse(result.isConsumed());
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should return expired result when token is expired")
    void testValidateTokenWithDetailsExpired() {
        token.setExpiresAt(Instant.now().minus(Duration.ofHours(1)));
        token.setConsumed(false);
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.of(token));

        TokenService.TokenValidationResult result = tokenService.validateTokenWithDetails(tokenValue, tokenType);

        assertFalse(result.isValid());
        assertTrue(result.isExpired());
        assertFalse(result.isConsumed());
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should return consumed result when token is consumed")
    void testValidateTokenWithDetailsConsumed() {
        token.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        token.setConsumed(true);
        token.setConsumedAt(Instant.now());
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.of(token));

        TokenService.TokenValidationResult result = tokenService.validateTokenWithDetails(tokenValue, tokenType);

        assertFalse(result.isValid());
        assertFalse(result.isExpired());
        assertTrue(result.isConsumed());
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should invalidate token successfully")
    void testInvalidateTokenSuccessfully() {
        token.setConsumed(false);
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(Token.class))).thenReturn(token);

        boolean invalidated = tokenService.invalidateToken(tokenValue);

        assertTrue(invalidated);
        assertTrue(token.isConsumed());
        assertNotNull(token.getConsumedAt());
        verify(tokenRepository).findByToken(tokenValue);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("Should return false when token not found for invalidation")
    void testInvalidateTokenNotFound() {
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        boolean invalidated = tokenService.invalidateToken(tokenValue);

        assertFalse(invalidated);
        verify(tokenRepository).findByToken(tokenValue);
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    @DisplayName("Should return false when token already consumed")
    void testInvalidateTokenAlreadyConsumed() {
        token.setConsumed(true);
        token.setConsumedAt(Instant.now());
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        boolean invalidated = tokenService.invalidateToken(tokenValue);

        assertFalse(invalidated);
        verify(tokenRepository).findByToken(tokenValue);
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    @DisplayName("Should cleanup expired tokens successfully")
    void testCleanupExpiredTokens() {
        tokenService.cleanupExpiredTokens();

        verify(tokenRepository).deleteByExpiresAtBefore(any(Instant.class));
        verify(tokenRepository).deleteByIsConsumedTrueAndExpiresAtBefore(any(Instant.class));
    }
}
