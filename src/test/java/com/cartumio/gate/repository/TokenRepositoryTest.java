package com.cartumio.gate.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.domain.token.Token;
import com.cartumio.gate.domain.token.TokenType;

@DisplayName("TokenRepository - Tests")
class TokenRepositoryTest {

    private TokenRepository tokenRepository;
    private Token token;
    private String tokenValue;
    private TokenType tokenType;
    private UUID tokenId;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(TokenRepository.class);
        tokenValue = UUID.randomUUID().toString();
        tokenType = TokenType.EMAIL_CONFIRMATION;
        tokenId = UUID.randomUUID();
        expiresAt = Instant.now().plusSeconds(86400);

        token = new Token();
        token.setId(tokenId);
        token.setToken(tokenValue);
        token.setTokenType(tokenType);
        token.setExpiresAt(expiresAt);
        token.setConsumed(false);
    }

    @Test
    @DisplayName("Should save token successfully")
    void testSaveTokenSuccessfully() {
        when(tokenRepository.save(token)).thenReturn(token);
        tokenRepository.save(token);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("Should find token by token value successfully")
    void testFindByTokenSuccessfully() {
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        tokenRepository.findByToken(tokenValue);
        verify(tokenRepository).findByToken(tokenValue);
    }

    @Test
    @DisplayName("Should return empty when token not found")
    void testFindByTokenNotFound() {
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());
        tokenRepository.findByToken(tokenValue);
        verify(tokenRepository).findByToken(tokenValue);
    }

    @Test
    @DisplayName("Should check if token exists successfully")
    void testExistsByTokenSuccessfully() {
        when(tokenRepository.existsByToken(tokenValue)).thenReturn(true);
        tokenRepository.existsByToken(tokenValue);
        verify(tokenRepository).existsByToken(tokenValue);
    }

    @Test
    @DisplayName("Should return false when token does not exist")
    void testExistsByTokenNotFound() {
        when(tokenRepository.existsByToken(tokenValue)).thenReturn(false);
        tokenRepository.existsByToken(tokenValue);
        verify(tokenRepository).existsByToken(tokenValue);
    }

    @Test
    @DisplayName("Should find token by token value and type successfully")
    void testFindByTokenAndTokenTypeSuccessfully() {
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.of(token));
        tokenRepository.findByTokenAndTokenType(tokenValue, tokenType);
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should return empty when token with type not found")
    void testFindByTokenAndTokenTypeNotFound() {
        when(tokenRepository.findByTokenAndTokenType(tokenValue, tokenType))
                .thenReturn(Optional.empty());
        tokenRepository.findByTokenAndTokenType(tokenValue, tokenType);
        verify(tokenRepository).findByTokenAndTokenType(tokenValue, tokenType);
    }

    @Test
    @DisplayName("Should delete expired tokens successfully")
    void testDeleteByExpiresAtBefore() {
        Instant now = Instant.now();
        tokenRepository.deleteByExpiresAtBefore(now);
        verify(tokenRepository).deleteByExpiresAtBefore(now);
    }

    @Test
    @DisplayName("Should delete consumed and expired tokens successfully")
    void testDeleteByIsConsumedTrueAndExpiresAtBefore() {
        Instant now = Instant.now();
        tokenRepository.deleteByIsConsumedTrueAndExpiresAtBefore(now);
        verify(tokenRepository).deleteByIsConsumedTrueAndExpiresAtBefore(now);
    }
}
