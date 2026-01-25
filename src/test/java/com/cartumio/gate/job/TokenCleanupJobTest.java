package com.cartumio.gate.job;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cartumio.gate.service.token.TokenService;

@DisplayName("TokenCleanupJob - Tests")
class TokenCleanupJobTest {

    private TokenCleanupJob tokenCleanupJob;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        tokenCleanupJob = new TokenCleanupJob(tokenService);
    }

    @Test
    @DisplayName("Should cleanup expired tokens successfully")
    void testCleanupExpiredTokensSuccessfully() {
        tokenCleanupJob.cleanupExpiredTokens();

        verify(tokenService).cleanupExpiredTokens();
    }

    @Test
    @DisplayName("Should handle exception during cleanup gracefully")
    void testCleanupExpiredTokensHandlesException() {
        doThrow(new RuntimeException("Database error")).when(tokenService).cleanupExpiredTokens();

        try {
            tokenCleanupJob.cleanupExpiredTokens();
        } catch (Exception e) {
            // Exception should be caught and logged internally
        }

        verify(tokenService).cleanupExpiredTokens();
    }
}
